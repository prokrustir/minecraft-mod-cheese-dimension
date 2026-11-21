package pl.maciej.cheesedimension.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

/**
 * A friendly character that talks when right-clicked. It never despawns, cannot be hurt (only /kill
 * or the void) and always shows its name. Each click sends the next dialogue line
 * ({@code <dialogueKey>.1}, {@code .2}, ...) from the language files to the player in chat.
 */
public abstract class TalkingNpcEntity extends PathAwareEntity {
	private final String dialogueKey;
	private final int dialogueLines;
	private final int loopFromLine;
	private final float voicePitch;

	/** Which line each player hears next. Not saved: after a restart the conversation starts over. */
	private final Map<UUID, Integer> nextLine = new HashMap<>();

	/**
	 * @param dialogueKey   translation key prefix of the lines, e.g. "dialogue.cheesedimension.rat_king"
	 * @param dialogueLines number of lines in the language files
	 * @param loopFromLine  line to continue with after the last one
	 * @param voicePitch    pitch of the villager sound played when talking
	 */
	protected TalkingNpcEntity(EntityType<? extends TalkingNpcEntity> entityType, World world,
							   String dialogueKey, int dialogueLines, int loopFromLine, float voicePitch) {
		super(entityType, world);
		this.dialogueKey = dialogueKey;
		this.dialogueLines = dialogueLines;
		this.loopFromLine = loopFromLine;
		this.voicePitch = voicePitch;
		setPersistent();
		setInvulnerable(true);
		setCustomName(entityType.getName());
		setCustomNameVisible(true);
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
				.append(Text.translatable(dialogueKey + "." + index));
		player.sendMessage(message, false);
		getLookControl().lookAt(player);
		getNavigation().stop();
		playSound(SoundEvents.ENTITY_VILLAGER_AMBIENT, 1.0f, voicePitch);
		nextLine.put(player.getUuid(), index >= dialogueLines ? loopFromLine : index + 1);
	}
}
