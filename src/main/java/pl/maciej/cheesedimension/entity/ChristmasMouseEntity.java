package pl.maciej.cheesedimension.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

/**
 * A friendly humanoid mouse in Christmas clothes who lives in the Christmas House. It walks slowly
 * around the house (it cannot open the door) and wishes a Merry Christmas when right-clicked.
 *
 * <p>Used by two entity types, the elf mouse and the Christmas mouse. Their dialogue lines are
 * "dialogue.cheesedimension.&lt;entity id&gt;.1" to ".4"; after the last line the conversation starts again.
 */
public class ChristmasMouseEntity extends TalkingNpcEntity {
	private static final int DIALOGUE_LINES = 4;

	public ChristmasMouseEntity(EntityType<? extends ChristmasMouseEntity> entityType, World world) {
		super(entityType, world, dialogueKey(entityType), DIALOGUE_LINES, 1, 1.5f);
	}

	private static String dialogueKey(EntityType<?> entityType) {
		Identifier id = Registries.ENTITY_TYPE.getId(entityType);
		return "dialogue." + id.getNamespace() + "." + id.getPath();
	}

	public static DefaultAttributeContainer.Builder createChristmasMouseAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected void initGoals() {
		goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
		// Slow, occasional walks inside the house.
		goalSelector.add(2, new WanderAroundGoal(this, 0.6, 200));
		goalSelector.add(3, new LookAroundGoal(this));
	}
}
