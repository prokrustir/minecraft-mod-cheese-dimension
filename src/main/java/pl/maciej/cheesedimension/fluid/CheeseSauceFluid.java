package pl.maciej.cheesedimension.fluid;

import java.util.Optional;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.ModFluids;
import pl.maciej.cheesedimension.ModItems;

/** Cheese sauce: behaves like water, but flows a bit slower. */
public abstract class CheeseSauceFluid extends FlowableFluid {
	@Override
	public Fluid getFlowing() {
		return ModFluids.FLOWING_CHEESE_SAUCE;
	}

	@Override
	public Fluid getStill() {
		return ModFluids.STILL_CHEESE_SAUCE;
	}

	@Override
	public Item getBucketItem() {
		return ModItems.CHEESE_SAUCE_BUCKET;
	}

	@Override
	protected boolean isInfinite(World world) {
		// Same rule as water: two adjacent sources make a new source.
		return world.getGameRules().getBoolean(GameRules.WATER_SOURCE_CONVERSION);
	}

	@Override
	protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
		state.getBlock().dropStacks(state, world, pos, state.hasBlockEntity() ? world.getBlockEntity(pos) : null);
	}

	@Override
	protected int getMaxFlowDistance(WorldView world) {
		return 4;
	}

	@Override
	protected int getLevelDecreasePerBlock(WorldView world) {
		return 1;
	}

	@Override
	public int getTickRate(WorldView world) {
		return 10;
	}

	@Override
	protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
		return false;
	}

	@Override
	protected float getBlastResistance() {
		return 100.0f;
	}

	@Override
	protected BlockState toBlockState(FluidState state) {
		return ModBlocks.CHEESE_SAUCE.getDefaultState().with(FluidBlock.LEVEL, getBlockStateLevel(state));
	}

	@Override
	public Optional<SoundEvent> getBucketFillSound() {
		return Optional.of(SoundEvents.ITEM_BUCKET_FILL);
	}

	@Override
	public boolean matchesType(Fluid fluid) {
		return fluid == ModFluids.STILL_CHEESE_SAUCE || fluid == ModFluids.FLOWING_CHEESE_SAUCE;
	}

	public static class Flowing extends CheeseSauceFluid {
		@Override
		protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
			super.appendProperties(builder);
			builder.add(LEVEL);
		}

		@Override
		public int getLevel(FluidState state) {
			return state.get(LEVEL);
		}

		@Override
		public boolean isStill(FluidState state) {
			return false;
		}
	}

	public static class Still extends CheeseSauceFluid {
		@Override
		public int getLevel(FluidState state) {
			return 8;
		}

		@Override
		public boolean isStill(FluidState state) {
			return true;
		}
	}
}
