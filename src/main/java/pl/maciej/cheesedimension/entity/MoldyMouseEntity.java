package pl.maciej.cheesedimension.entity;

import java.util.EnumSet;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/** A mouse that ate moldy cheese: it chases the nearest player and explodes like a creeper. */
public class MoldyMouseEntity extends HostileEntity {
	private static final TrackedData<Integer> FUSE_SPEED =
			DataTracker.registerData(MoldyMouseEntity.class, TrackedDataHandlerRegistry.INTEGER);

	/** Ticks of fuse before the explosion (30 = 1.5 s, like a creeper). */
	private static final int FUSE_TIME = 30;
	private static final float EXPLOSION_POWER = 2.0f;

	private int lastFuseTime;
	private int currentFuseTime;

	public MoldyMouseEntity(EntityType<? extends MoldyMouseEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createMoldyMouseAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 6.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.32);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(2, new IgniteGoal(this));
		goalSelector.add(4, new ChaseTargetGoal(this, 1.1));
		goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		goalSelector.add(6, new LookAroundGoal(this));
		targetSelector.add(1, new TargetAnyPlayerGoal(this));
		targetSelector.add(2, new RevengeGoal(this));
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		super.initDataTracker(builder);
		builder.add(FUSE_SPEED, -1);
	}

	@Override
	public void tick() {
		if (isAlive()) {
			lastFuseTime = currentFuseTime;
			int speed = getFuseSpeed();
			if (speed > 0 && currentFuseTime == 0) {
				playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 1.2f);
			}
			currentFuseTime += speed;
			if (currentFuseTime < 0) {
				currentFuseTime = 0;
			}
			if (currentFuseTime >= FUSE_TIME) {
				currentFuseTime = FUSE_TIME;
				explode();
			}
		}
		super.tick();
	}

	private void explode() {
		if (!getWorld().isClient) {
			// ExplosionSourceType.MOB respects the mobGriefing game rule, as for a creeper.
			getWorld().createExplosion(this, getX(), getY(), getZ(), EXPLOSION_POWER, World.ExplosionSourceType.MOB);
			discard();
		}
	}

	public int getFuseSpeed() {
		return dataTracker.get(FUSE_SPEED);
	}

	public void setFuseSpeed(int speed) {
		dataTracker.set(FUSE_SPEED, speed);
	}

	public float getClientFuseTime(float tickDelta) {
		return MathHelper.lerp(tickDelta, lastFuseTime, currentFuseTime) / (FUSE_TIME - 2);
	}

	// Sounds are borrowed from the rabbit, like for the normal mouse.
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

	/**
	 * Runs towards the target. The vanilla MeleeAttackGoal is not used because it stops
	 * as soon as the target is a creative-mode player.
	 */
	private static class ChaseTargetGoal extends Goal {
		private final MoldyMouseEntity mouse;
		private final double speed;
		private int repathDelay;

		ChaseTargetGoal(MoldyMouseEntity mouse, double speed) {
			this.mouse = mouse;
			this.speed = speed;
			setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		@Override
		public boolean canStart() {
			LivingEntity target = mouse.getTarget();
			return target != null && target.isAlive();
		}

		@Override
		public boolean shouldContinue() {
			return canStart();
		}

		@Override
		public void start() {
			repathDelay = 0;
		}

		@Override
		public void stop() {
			mouse.getNavigation().stop();
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			LivingEntity target = mouse.getTarget();
			if (target == null) {
				return;
			}
			mouse.getLookControl().lookAt(target, 30.0f, 30.0f);
			if (--repathDelay <= 0) {
				repathDelay = 4 + mouse.getRandom().nextInt(7);
				mouse.getNavigation().startMovingTo(target, speed);
			}
		}
	}

	/**
	 * Targets the nearest visible player, including players in creative mode (the vanilla
	 * ActiveTargetGoal skips them, so a creeper ignores creative players). Spectators are still ignored.
	 */
	private static class TargetAnyPlayerGoal extends Goal {
		private final MoldyMouseEntity mouse;
		private PlayerEntity target;

		TargetAnyPlayerGoal(MoldyMouseEntity mouse) {
			this.mouse = mouse;
			setControls(EnumSet.of(Control.TARGET));
		}

		private double range() {
			return mouse.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
		}

		@Override
		public boolean canStart() {
			// ignoreCreative = false: creative players are valid targets.
			PlayerEntity player = mouse.getWorld().getClosestPlayer(mouse.getX(), mouse.getY(), mouse.getZ(), range(), false);
			if (player == null || !mouse.getVisibilityCache().canSee(player)) {
				return false;
			}
			target = player;
			return true;
		}

		@Override
		public boolean shouldContinue() {
			return target != null && target.isAlive() && !target.isSpectator()
					&& mouse.squaredDistanceTo(target) <= range() * range();
		}

		@Override
		public void start() {
			mouse.setTarget(target);
		}

		@Override
		public void stop() {
			mouse.setTarget(null);
			target = null;
		}
	}

	/** Starts the fuse when the target is close, cancels it when the target runs away (as in CreeperIgniteGoal). */
	private static class IgniteGoal extends Goal {
		private final MoldyMouseEntity mouse;
		private LivingEntity target;

		IgniteGoal(MoldyMouseEntity mouse) {
			this.mouse = mouse;
			setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			LivingEntity candidate = mouse.getTarget();
			return mouse.getFuseSpeed() > 0
					|| candidate != null && mouse.squaredDistanceTo(candidate) < 9.0;
		}

		@Override
		public void start() {
			mouse.getNavigation().stop();
			target = mouse.getTarget();
		}

		@Override
		public void stop() {
			target = null;
		}

		@Override
		public boolean shouldRunEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			if (target == null || mouse.squaredDistanceTo(target) > 49.0 || !mouse.getVisibilityCache().canSee(target)) {
				mouse.setFuseSpeed(-1);
			} else {
				mouse.setFuseSpeed(1);
			}
		}
	}
}
