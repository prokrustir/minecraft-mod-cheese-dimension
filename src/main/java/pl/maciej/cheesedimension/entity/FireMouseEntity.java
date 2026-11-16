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
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A mouse that ate cheddar: it is red, immune to fire, and shoots small fireballs (like a blaze)
 * at the nearest player, also in creative mode.
 */
public class FireMouseEntity extends HostileEntity {
	public FireMouseEntity(EntityType<? extends FireMouseEntity> entityType, World world) {
		super(entityType, world);
	}

	public static DefaultAttributeContainer.Builder createFireMouseAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 8.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new SwimGoal(this));
		goalSelector.add(3, new ShootFireballGoal(this));
		goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
		goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		goalSelector.add(6, new LookAroundGoal(this));
		targetSelector.add(1, new TargetAnyPlayerGoal(this));
		targetSelector.add(2, new RevengeGoal(this));
	}

	@Override
	public void tickMovement() {
		if (getWorld().isClient && random.nextInt(6) == 0) {
			getWorld().addParticle(ParticleTypes.SMALL_FLAME, getParticleX(0.4), getRandomBodyY(), getParticleZ(0.4), 0.0, 0.01, 0.0);
		}
		super.tickMovement();
	}

	private void shootFireballAt(LivingEntity target) {
		double dx = target.getX() - getX();
		double dy = target.getBodyY(0.5) - (getY() + 0.4);
		double dz = target.getZ() - getZ();
		SmallFireballEntity fireball = new SmallFireballEntity(getWorld(), this, new Vec3d(dx, dy, dz).normalize());
		// Start a little above the small mouse so the fireball does not hit the ground right away.
		fireball.setPosition(getX(), getY() + 0.4, getZ());
		getWorld().spawnEntity(fireball);
		playSound(SoundEvents.ENTITY_BLAZE_SHOOT, 1.0f, 1.4f);
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
	 * Walks towards the target until it is close enough and visible, then stands still and shoots a
	 * fireball every 1.5 to 2.5 seconds. Written by hand because the vanilla ranged goals stop when
	 * the target is a creative-mode player.
	 */
	private static class ShootFireballGoal extends Goal {
		private static final double SHOOT_RANGE = 12.0;

		private final FireMouseEntity mouse;
		private int cooldown;
		private int repathDelay;

		ShootFireballGoal(FireMouseEntity mouse) {
			this.mouse = mouse;
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
			cooldown = 20;
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
			boolean visible = mouse.getVisibilityCache().canSee(target);
			boolean inRange = mouse.squaredDistanceTo(target) < SHOOT_RANGE * SHOOT_RANGE;
			if (visible && inRange) {
				mouse.getNavigation().stop();
			} else if (--repathDelay <= 0) {
				repathDelay = 4 + mouse.getRandom().nextInt(7);
				mouse.getNavigation().startMovingTo(target, 1.1);
			}
			if (cooldown > 0) {
				cooldown--;
			} else if (visible && inRange) {
				mouse.shootFireballAt(target);
				cooldown = 30 + mouse.getRandom().nextInt(21);
			}
		}
	}
}
