package pl.maciej.cheesedimension.portal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.block.CheesePortalBlock;

/**
 * Wykrywanie ramy portalu, zapalanie jej oraz szukanie lub budowanie portalu po drugiej stronie.
 */
public final class CheesePortalHelper {
	private static final int MIN_WIDTH = 2;
	private static final int MAX_WIDTH = 21;
	private static final int MIN_HEIGHT = 3;
	private static final int MAX_HEIGHT = 21;
	private static final int SEARCH_RADIUS = 8;

	private CheesePortalHelper() {
	}

	private record Frame(BlockPos bottomLeft, Direction.Axis axis, int width, int height) {
	}

	private static boolean isCheese(World world, BlockPos pos) {
		return world.getBlockState(pos).isOf(ModBlocks.CHEESE_BLOCK);
	}

	private static boolean isEmpty(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.isAir() || state.isOf(Blocks.FIRE);
	}

	/**
	 * Probuje zapalic portal. {@code start} to puste pole przy kliknietym bloku ramy.
	 * Sprawdza obie osie. Zwraca true, jesli portal powstal.
	 */
	public static boolean tryLight(World world, BlockPos start) {
		for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
			Frame frame = detect(world, start, axis);
			if (frame != null) {
				fill(world, frame);
				return true;
			}
		}
		return false;
	}

	@Nullable
	private static Frame detect(World world, BlockPos start, Direction.Axis axis) {
		Direction right = Direction.get(Direction.AxisDirection.POSITIVE, axis);
		Direction left = right.getOpposite();

		// 1. zejdz do podlogi z sera
		BlockPos pos = start;
		int down = 0;
		while (down < MAX_HEIGHT && isEmpty(world, pos.down())) {
			pos = pos.down();
			down++;
		}
		if (!isEmpty(world, pos) || !isCheese(world, pos.down())) {
			return null;
		}

		// 2. idz w lewo do ramy
		int steps = 0;
		while (steps < MAX_WIDTH && isEmpty(world, pos.offset(left)) && isCheese(world, pos.offset(left).down())) {
			pos = pos.offset(left);
			steps++;
		}
		if (!isCheese(world, pos.offset(left))) {
			return null;
		}
		BlockPos bottomLeft = pos;

		// 3. zmierz szerokosc
		int width = 1;
		while (width <= MAX_WIDTH
				&& isEmpty(world, bottomLeft.offset(right, width))
				&& isCheese(world, bottomLeft.offset(right, width).down())) {
			width++;
		}
		if (!isCheese(world, bottomLeft.offset(right, width)) || width < MIN_WIDTH || width > MAX_WIDTH) {
			return null;
		}

		// 4. zmierz wysokosc: kazdy rzad pusty, po bokach ser
		int height = 0;
		while (height < MAX_HEIGHT) {
			boolean rowEmpty = true;
			for (int i = 0; i < width; i++) {
				if (!isEmpty(world, bottomLeft.offset(right, i).up(height))) {
					rowEmpty = false;
					break;
				}
			}
			if (!rowEmpty) {
				break;
			}
			if (!isCheese(world, bottomLeft.offset(left).up(height))
					|| !isCheese(world, bottomLeft.offset(right, width).up(height))) {
				return null;
			}
			height++;
		}
		if (height < MIN_HEIGHT || height >= MAX_HEIGHT) {
			return null;
		}

		// 5. gorna belka z sera
		for (int i = 0; i < width; i++) {
			if (!isCheese(world, bottomLeft.offset(right, i).up(height))) {
				return null;
			}
		}
		return new Frame(bottomLeft, axis, width, height);
	}

	private static void fill(World world, Frame frame) {
		Direction right = Direction.get(Direction.AxisDirection.POSITIVE, frame.axis());
		BlockState portal = ModBlocks.CHEESE_PORTAL.getDefaultState().with(CheesePortalBlock.AXIS, frame.axis());
		for (int i = 0; i < frame.width(); i++) {
			for (int j = 0; j < frame.height(); j++) {
				BlockPos p = frame.bottomLeft().offset(right, i).up(j);
				world.setBlockState(p, portal, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
			}
		}
	}

	// ------------------------------------------------------------------
	// Portal po drugiej stronie
	// ------------------------------------------------------------------

	/**
	 * Zwraca pozycje, w ktorej ma sie pojawic encja. Jesli w poblizu nie ma portalu, buduje nowy.
	 *
	 * @param origin pozycja bloku portalu, z ktorego wchodzi encja (wspolrzedne przenosimy 1:1)
	 */
	public static Vec3d findOrCreatePortal(ServerWorld destination, BlockPos origin) {
		BlockPos found = findExisting(destination, origin);
		if (found != null) {
			return Vec3d.ofBottomCenter(found);
		}
		return build(destination, origin);
	}

	@Nullable
	private static BlockPos findExisting(ServerWorld world, BlockPos origin) {
		int bottom = world.getBottomY();
		int top = world.getTopY();
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		BlockPos.Mutable cursor = new BlockPos.Mutable();

		for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
			for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
				int x = origin.getX() + dx;
				int z = origin.getZ() + dz;
				for (int y = top - 1; y >= bottom; y--) {
					cursor.set(x, y, z);
					if (world.getBlockState(cursor).isOf(ModBlocks.CHEESE_PORTAL)) {
						double dist = dx * dx + dz * dz + 0.01 * Math.abs(y - origin.getY());
						if (dist < bestDist) {
							bestDist = dist;
							best = cursor.toImmutable();
						}
					}
				}
			}
		}
		if (best == null) {
			return null;
		}
		// zejdz do dolnego bloku portalu
		while (world.getBlockState(best.down()).isOf(ModBlocks.CHEESE_PORTAL)) {
			best = best.down();
		}
		return best;
	}

	/** Buduje ramke 4x5 (wnetrze 2x3) z platforma, na powierzchni terenu. */
	private static Vec3d build(ServerWorld world, BlockPos origin) {
		int x = origin.getX();
		int z = origin.getZ();
		int surface = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
		int minY = world.getBottomY() + 6;
		int maxY = world.getTopY() - 10;
		int py = MathHelper.clamp(surface <= world.getBottomY() + 1 ? 80 : surface, minY, maxY);

		BlockState cheese = ModBlocks.CHEESE_BLOCK.getDefaultState();
		BlockState portal = ModBlocks.CHEESE_PORTAL.getDefaultState().with(CheesePortalBlock.AXIS, Direction.Axis.X);
		BlockState air = Blocks.AIR.getDefaultState();
		int flags = Block.NOTIFY_LISTENERS | Block.FORCE_STATE;

		// oczysc przestrzen i postaw podloge: x w [x-1, x+2], z w [z-1, z+1]
		for (int dx = -1; dx <= 2; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				world.setBlockState(new BlockPos(x + dx, py - 1, z + dz), cheese, flags);
				for (int dy = 0; dy <= 3; dy++) {
					world.setBlockState(new BlockPos(x + dx, py + dy, z + dz), air, flags);
				}
			}
		}
		// rama: dol i gora
		for (int dx = -1; dx <= 2; dx++) {
			world.setBlockState(new BlockPos(x + dx, py + 3, z), cheese, flags);
		}
		// rama: boki
		for (int dy = 0; dy <= 2; dy++) {
			world.setBlockState(new BlockPos(x - 1, py + dy, z), cheese, flags);
			world.setBlockState(new BlockPos(x + 2, py + dy, z), cheese, flags);
		}
		// wnetrze
		for (int dx = 0; dx <= 1; dx++) {
			for (int dy = 0; dy <= 2; dy++) {
				world.setBlockState(new BlockPos(x + dx, py + dy, z), portal, flags);
			}
		}
		return new Vec3d(x + 1.0, py, z + 0.5);
	}
}
