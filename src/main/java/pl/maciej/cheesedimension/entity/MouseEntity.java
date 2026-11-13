package pl.maciej.cheesedimension.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.ModEntities;
import pl.maciej.cheesedimension.ModItems;

/** Mala mysz, ktora zyje w serowym wymiarze, lubi ser i rozmnaza sie nim. */
public class MouseEntity extends AnimalEntity {
	/** Ticks between eating moldy cheese and turning into a moldy mouse (100 = 5 s), so the player can get away. */
	private static final int INFECTION_DELAY = 100;

	/** -1 when healthy, otherwise ticks left until the transformation. */
	private int infectionTicks = -1;

	public MouseEntity(EntityType<? extends MouseEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createMouseAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(0, new SwimGoal(this));
		goalSelector.add(1, new EscapeDangerGoal(this, 1.4));
		goalSelector.add(2, new AnimalMateGoal(this, 1.0));
		goalSelector.add(3, new TemptGoal(this, 1.2, stack -> stack.isOf(ModItems.CHEESE), false));
		goalSelector.add(4, new FollowParentGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 1.0));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		goalSelector.add(7, new LookAroundGoal(this));
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return stack.isOf(ModItems.CHEESE);
	}

	/** Feeding moldy cheese infects the mouse; it turns into a moldy mouse after a short delay. */
	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (stack.isOf(ModItems.MOLDY_CHEESE)) {
			if (infectionTicks >= 0) {
				return ActionResult.PASS;
			}
			if (!getWorld().isClient) {
				infectionTicks = INFECTION_DELAY;
				stack.decrementUnlessCreative(1, player);
				playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0f, 1.0f);
			}
			return ActionResult.success(getWorld().isClient);
		}
		return super.interactMob(player, hand);
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		if (infectionTicks < 0 || !(getWorld() instanceof ServerWorld world)) {
			return;
		}
		if (infectionTicks > 0) {
			infectionTicks--;
			if (infectionTicks % 5 == 0) {
				world.spawnParticles(ParticleTypes.COMPOSTER, getX(), getBodyY(0.5), getZ(), 3, 0.2, 0.2, 0.2, 0.02);
			}
			return;
		}
		MoldyMouseEntity moldy = convertTo(ModEntities.MOLDY_MOUSE, true);
		if (moldy != null) {
			moldy.setPersistent();
			moldy.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 1.5f);
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		nbt.putInt("InfectionTicks", infectionTicks);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		infectionTicks = nbt.contains("InfectionTicks") ? nbt.getInt("InfectionTicks") : -1;
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		return ModEntities.MOUSE.create(world);
	}

	/** Myszy wola chodzic po serze (domyslnie zwierzeta wola trawe). */
	@Override
	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		if (world.getBlockState(pos.down()).isOf(ModBlocks.CHEESE_BLOCK)) {
			return 10.0f;
		}
		return super.getPathfindingFavor(pos, world);
	}

	/** Myszy pojawiaja sie tylko na bloku sera. */
	public static boolean canSpawnOnCheese(EntityType<MouseEntity> type, WorldAccess world, SpawnReason reason,
								   BlockPos pos, Random random) {
		return world.getBlockState(pos.down()).isOf(ModBlocks.CHEESE_BLOCK);
	}

	// Dzwieki tymczasowo pozyczone od krolika.
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
