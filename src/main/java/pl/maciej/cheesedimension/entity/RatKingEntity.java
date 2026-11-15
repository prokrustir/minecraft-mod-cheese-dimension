package pl.maciej.cheesedimension.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pl.maciej.cheesedimension.ModEntities;

/**
 * The Rat King: a big boss mouse that lives in the cheese castle. It fights in melee and,
 * while it has a target, regularly summons moldy mice (which do not break blocks).
 */
public class RatKingEntity extends HostileEntity {
	/** Ticks between summons (200 = 10 s). */
	private static final int SUMMON_INTERVAL = 200;
	private static final int MINIONS_PER_SUMMON = 2;
	/** No new minions are summoned while this many moldy mice are already near the king. */
	private static final int MAX_MINIONS_NEARBY = 4;

	private final ServerBossBar bossBar =
			new ServerBossBar(getDisplayName(), BossBar.Color.YELLOW, BossBar.Style.PROGRESS);
	private int summonCooldown = 100;

	public RatKingEntity(EntityType<? extends RatKingEntity> entityType, World world) {
		super(entityType, world);
		experiencePoints = 50;
		// A boss must never despawn when the player walks away.
		setPersistent();
	}

	public static DefaultAttributeContainer.Builder createRatKingAttributes() {
		return HostileEntity.createHostileAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 120.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.28)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 7.0)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(2, new MeleeAttackGoal(this, 1.1, false));
		goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		goalSelector.add(6, new LookAroundGoal(this));
		targetSelector.add(1, new RevengeGoal(this));
		targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		bossBar.setPercent(getHealth() / getMaxHealth());
		if (getTarget() != null && --summonCooldown <= 0) {
			summonCooldown = SUMMON_INTERVAL;
			summonMinions();
		}
	}

	private void summonMinions() {
		if (!(getWorld() instanceof ServerWorld world)) {
			return;
		}
		int nearby = world.getEntitiesByType(ModEntities.MOLDY_MOUSE, getBoundingBox().expand(16.0), e -> true).size();
		if (nearby >= MAX_MINIONS_NEARBY) {
			return;
		}
		playSound(SoundEvents.ENTITY_EVOKER_PREPARE_SUMMON, 1.0f, 1.0f);
		for (int i = 0; i < MINIONS_PER_SUMMON; i++) {
			MoldyMouseEntity minion = ModEntities.MOLDY_MOUSE.create(world);
			if (minion == null) {
				continue;
			}
			minion.refreshPositionAndAngles(
					getX() + (random.nextDouble() - 0.5) * 3.0,
					getY(),
					getZ() + (random.nextDouble() - 0.5) * 3.0,
					random.nextFloat() * 360.0f, 0.0f);
			minion.setBlockSafe(true);
			minion.setTarget(getTarget());
			world.spawnEntity(minion);
		}
	}

	@Override
	public void setCustomName(@Nullable Text name) {
		super.setCustomName(name);
		bossBar.setName(getDisplayName());
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		bossBar.addPlayer(player);
	}

	@Override
	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		bossBar.removePlayer(player);
	}

	/** The king squeaks lower than a normal mouse. */
	@Override
	public float getSoundPitch() {
		return super.getSoundPitch() * 0.5f;
	}

	// Sounds are borrowed from the rabbit, like for the other mice.
	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_RABBIT_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_RABBIT_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_RABBIT_DEATH;
	}
}
