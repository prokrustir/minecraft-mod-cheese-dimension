package pl.maciej.cheesedimension.item;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.ModDimensions;
import pl.maciej.cheesedimension.portal.CheesePortalHelper;

/** Nóż do sera: kliknij nim blok ramy portalu, aby go uruchomić. */
public class CheeseKnifeItem extends Item {
	public CheeseKnifeItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		BlockPos clicked = context.getBlockPos();
		if (!world.getBlockState(clicked).isOf(ModBlocks.CHEESE_BLOCK)) {
			return ActionResult.PASS;
		}
		if (world.getRegistryKey() != World.OVERWORLD && world.getRegistryKey() != ModDimensions.CHEESE_WORLD) {
			return ActionResult.PASS;
		}

		BlockPos start = clicked.offset(context.getSide());
		if (!CheesePortalHelper.tryLight(world, start)) {
			return ActionResult.PASS;
		}

		world.playSound(null, start, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0f, 1.0f);
		PlayerEntity player = context.getPlayer();
		if (player != null && !world.isClient) {
			EquipmentSlot slot = context.getHand() == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
			context.getStack().damage(1, player, slot);
		}
		return ActionResult.success(world.isClient);
	}
}
