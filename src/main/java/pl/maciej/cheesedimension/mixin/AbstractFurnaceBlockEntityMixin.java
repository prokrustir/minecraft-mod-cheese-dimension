package pl.maciej.cheesedimension.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A furnace never returns the remainder of an ingredient, so smelting a milk bucket would swallow the
 * bucket. This mixin spits the empty bucket out of the front of the furnace when milk is smelted.
 */
@Mixin(AbstractFurnaceBlockEntity.class)
public class AbstractFurnaceBlockEntityMixin {
	private static final int INPUT_SLOT = 0;

	/** Number of milk buckets in the input slot at the start of the current furnace tick. */
	@Unique
	private static int cheesedimension$milkBefore;

	@Inject(method = "tick", at = @At("HEAD"))
	private static void cheesedimension$rememberMilk(World world, BlockPos pos, BlockState state,
													 AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
		ItemStack input = furnace.getStack(INPUT_SLOT);
		cheesedimension$milkBefore = input.isOf(Items.MILK_BUCKET) ? input.getCount() : 0;
	}

	@Inject(method = "tick", at = @At("RETURN"))
	private static void cheesedimension$returnBucket(World world, BlockPos pos, BlockState state,
													 AbstractFurnaceBlockEntity furnace, CallbackInfo ci) {
		if (world.isClient || cheesedimension$milkBefore == 0) {
			return;
		}
		ItemStack input = furnace.getStack(INPUT_SLOT);
		int milkAfter = input.isOf(Items.MILK_BUCKET) ? input.getCount() : 0;
		int smelted = cheesedimension$milkBefore - milkAfter;
		cheesedimension$milkBefore = 0;

		for (int i = 0; i < smelted; i++) {
			Direction facing = state.contains(HorizontalFacingBlock.FACING)
					? state.get(HorizontalFacingBlock.FACING) : Direction.NORTH;
			ItemEntity bucket = new ItemEntity(world,
					pos.getX() + 0.5 + facing.getOffsetX() * 0.7,
					pos.getY() + 0.5,
					pos.getZ() + 0.5 + facing.getOffsetZ() * 0.7,
					new ItemStack(Items.BUCKET),
					facing.getOffsetX() * 0.1, 0.1, facing.getOffsetZ() * 0.1);
			world.spawnEntity(bucket);
		}
	}
}
