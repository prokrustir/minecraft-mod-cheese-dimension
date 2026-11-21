package pl.maciej.cheesedimension.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

/**
 * The Rat King: a friendly, humanoid ruler of the cheese castle. He does not fight; he stands in
 * the keep, looks at visitors and talks to them when right-clicked (8 lines; after the last one
 * he continues from line 2, because line 1 is the greeting).
 */
public class RatKingEntity extends TalkingNpcEntity {
	public RatKingEntity(EntityType<? extends RatKingEntity> entityType, World world) {
		super(entityType, world, "dialogue.cheesedimension.rat_king", 8, 2, 0.7f);
	}

	public static DefaultAttributeContainer.Builder createRatKingAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected void initGoals() {
		// No wandering and no combat goals: the king waits on his floor and watches visitors.
		goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
		goalSelector.add(2, new LookAroundGoal(this));
	}
}
