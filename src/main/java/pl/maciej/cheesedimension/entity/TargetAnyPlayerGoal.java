package pl.maciej.cheesedimension.entity;

import java.util.EnumSet;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Targets the nearest visible player, including players in creative mode (the vanilla
 * ActiveTargetGoal skips them, so a creeper ignores creative players). Spectators are still ignored.
 */
public class TargetAnyPlayerGoal extends Goal {
	private final MobEntity mob;
	private PlayerEntity target;

	public TargetAnyPlayerGoal(MobEntity mob) {
		this.mob = mob;
		setControls(EnumSet.of(Control.TARGET));
	}

	private double range() {
		return mob.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE);
	}

	@Override
	public boolean canStart() {
		// ignoreCreative = false: creative players are valid targets.
		PlayerEntity player = mob.getWorld().getClosestPlayer(mob.getX(), mob.getY(), mob.getZ(), range(), false);
		if (player == null || !mob.getVisibilityCache().canSee(player)) {
			return false;
		}
		target = player;
		return true;
	}

	@Override
	public boolean shouldContinue() {
		return target != null && target.isAlive() && !target.isSpectator()
				&& mob.squaredDistanceTo(target) <= range() * range();
	}

	@Override
	public void start() {
		mob.setTarget(target);
	}

	@Override
	public void stop() {
		mob.setTarget(null);
		target = null;
	}
}
