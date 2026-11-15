package pl.maciej.cheesedimension.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * The Rat King: a friendly, humanoid ruler of the cheese castle. He does not fight; he stands in
 * the keep, looks at visitors and talks to them when right-clicked.
 */
public class RatKingEntity extends PathAwareEntity {
	/** Number of lines "dialogue.cheesedimension.rat_king.N" in the language files. */
	private static final int DIALOGUE_LINES = 8;
	/** After the last line the conversation loops back to this one (line 1 is the greeting). */
	private static final int LOOP_FROM_LINE = 2;

	/** Which line each player hears next. Not saved: after a restart the conversation starts over. */
	private final Map<UUID, Integer> nextLine = new HashMap<>();

	public RatKingEntity(EntityType<? extends RatKingEntity> entityType, World world) {
		super(entityType, world);
		// He is a character, not a monster: he never despawns and cannot be hurt (only /kill or the void).
		setPersistent();
		setInvulnerable(true);
		setCustomName(Text.translatable("entity.cheesedimension.rat_king"));
		setCustomNameVisible(true);
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

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (hand != Hand.MAIN_HAND) {
			return ActionResult.PASS;
		}
		if (!getWorld().isClient) {
			talkTo(player);
		}
		return ActionResult.success(getWorld().isClient);
	}

	private void talkTo(PlayerEntity player) {
		int index = nextLine.getOrDefault(player.getUuid(), 1);
		Text message = Text.literal("<").append(getDisplayName()).append("> ")
				.append(Text.translatable("dialogue.cheesedimension.rat_king." + index));
		player.sendMessage(message, false);
		getLookControl().lookAt(player);
		playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0f, 0.7f);
		nextLine.put(player.getUuid(), index >= DIALOGUE_LINES ? LOOP_FROM_LINE : index + 1);
	}
}
