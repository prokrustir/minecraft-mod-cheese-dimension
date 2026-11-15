package pl.maciej.cheesedimension.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import pl.maciej.cheesedimension.ModItems;

/**
 * Cheese Golem: behaves exactly like an iron golem (neutral to players until hurt, attacks hostile
 * mobs, strong melee hit). It is only placed by the Cheesehenge structure (and the spawn egg), and
 * it is repaired with cheese instead of iron ingots.
 */
public class CheeseGolemEntity extends IronGolemEntity {
	private static final float CHEESE_HEAL = 20.0f;

	public CheeseGolemEntity(EntityType<? extends IronGolemEntity> entityType, World world) {
		super(entityType, world);
		setPersistent();
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (!stack.isOf(ModItems.CHEESE)) {
			return ActionResult.PASS;
		}
		if (getHealth() >= getMaxHealth()) {
			return ActionResult.PASS;
		}
		heal(CHEESE_HEAL);
		playSound(SoundEvents.ENTITY_IRON_GOLEM_REPAIR, 1.0f, 1.0f);
		stack.decrementUnlessCreative(1, player);
		return ActionResult.success(getWorld().isClient);
	}
}
