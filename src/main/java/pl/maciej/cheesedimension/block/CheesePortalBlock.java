package pl.maciej.cheesedimension.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Portal;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.ModDimensions;
import pl.maciej.cheesedimension.portal.CheesePortalHelper;

/**
 * Wnetrze serowego portalu. Dziala jak portal do Netheru: stoisz w srodku kilka sekund
 * i przenosisz sie miedzy Overworldem a serowym wymiarem.
 */
public class CheesePortalBlock extends Block implements Portal {
	public static final MapCodec<CheesePortalBlock> CODEC = createCodec(CheesePortalBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

	private static final VoxelShape X_SHAPE = Block.createCuboidShape(0.0, 0.0, 6.0, 16.0, 16.0, 10.0);
	private static final VoxelShape Z_SHAPE = Block.createCuboidShape(6.0, 0.0, 0.0, 10.0, 16.0, 16.0);

	public CheesePortalBlock(Settings settings) {
		super(settings);
		setDefaultState(getStateManager().getDefaultState().with(AXIS, Direction.Axis.X));
	}

	@Override
	protected MapCodec<? extends Block> getCodec() {
		return CODEC;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(AXIS) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	/** Portal gasnie, gdy ktorys element ramy w jego plaszczyznie zniknie. */
	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState,
												   WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		Direction.Axis changed = direction.getAxis();
		boolean inPlane = changed == Direction.Axis.Y || changed == state.get(AXIS);
		if (inPlane && !neighborState.isOf(this) && !neighborState.isOf(ModBlocks.CHEESE_BLOCK)) {
			return Blocks.AIR.getDefaultState();
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity.canUsePortals(false)) {
			entity.tryUsePortal(this, pos);
		}
	}

	@Override
	public int getPortalDelay(ServerWorld world, Entity entity) {
		if (entity instanceof PlayerEntity player) {
			return player.getAbilities().invulnerable ? 1 : 80;
		}
		return 0;
	}

	@Override
	public Portal.Effect getPortalEffect() {
		return Portal.Effect.CONFUSION;
	}

	@Nullable
	@Override
	public TeleportTarget createTeleportTarget(ServerWorld world, Entity entity, BlockPos pos) {
		var destKey = world.getRegistryKey() == ModDimensions.CHEESE_WORLD ? World.OVERWORLD : ModDimensions.CHEESE_WORLD;
		ServerWorld destination = world.getServer().getWorld(destKey);
		if (destination == null) {
			return null;
		}
		Vec3d target = CheesePortalHelper.findOrCreatePortal(destination, pos);
		return new TeleportTarget(
				destination,
				target,
				Vec3d.ZERO,
				entity.getYaw(),
				entity.getPitch(),
				TeleportTarget.SEND_TRAVEL_THROUGH_PORTAL_PACKET.then(TeleportTarget.ADD_PORTAL_CHUNK_TICKET));
	}
}
