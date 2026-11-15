package pl.maciej.cheesedimension.mixin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementCalculator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The cheese castle uses concentric ring placement (one castle per world, like a stronghold),
 * which picks its position before any terrain exists. Vanilla only nudges that position towards
 * the preferred biome, so it can still land on the cheese sauce sea, where the structure's biome
 * check then fails and the world has no castle at all.
 *
 * This mixin moves every castle ring position to the nearest chunk whose whole castle footprint is
 * Cheese Plains. Because the positions themselves are changed, generation and
 * {@code /locate structure} both use the corrected positions.
 */
@Mixin(StructurePlacementCalculator.class)
public abstract class StructurePlacementCalculatorMixin {
    @Unique
    private static final Identifier CHEESE_CASTLE_SET = Identifier.of("cheesedimension", "cheese_castle");
    @Unique
    private static final RegistryKey<Biome> CHEESE_PLAINS =
            RegistryKey.of(RegistryKeys.BIOME, Identifier.of("cheesedimension", "cheese_plains"));
    /** How many chunks away from the original position to search for land. */
    @Unique
    private static final int SEARCH_RADIUS_CHUNKS = 16;
    /** Half size of the area that must be land, in blocks. The castle is 20x20 and rotates around the chunk corner. */
    @Unique
    private static final int FOOTPRINT_HALF_SIZE = 24;
    @Unique
    private static final int SAMPLE_STEP = 4;
    /** Height used for biome sampling. The biomes only depend on continentalness, so any height works. */
    @Unique
    private static final int SAMPLE_Y = 90;

    @Shadow
    @Final
    private NoiseConfig noiseConfig;

    @Shadow
    @Final
    private BiomeSource biomeSource;

    @Inject(method = "calculateConcentricsRingPlacementPos", at = @At("RETURN"), cancellable = true)
    private void cheesedimension$moveCastleOntoLand(RegistryEntry<StructureSet> structureSetEntry,
                                                    ConcentricRingsStructurePlacement placement,
                                                    CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> cir) {
        if (!structureSetEntry.matchesId(CHEESE_CASTLE_SET)) {
            return;
        }
        CompletableFuture<List<ChunkPos>> original = cir.getReturnValue();
        cir.setReturnValue(original.thenApply(positions -> {
            List<ChunkPos> moved = new ArrayList<>(positions.size());
            for (ChunkPos pos : positions) {
                moved.add(cheesedimension$findLand(pos));
            }
            return moved;
        }));
    }

    /** Returns the chunk closest to {@code start} (in growing square rings) whose castle footprint is all Cheese Plains. */
    @Unique
    private ChunkPos cheesedimension$findLand(ChunkPos start) {
        MultiNoiseUtil.MultiNoiseSampler sampler = this.noiseConfig.getMultiNoiseSampler();
        for (int r = 0; r <= SEARCH_RADIUS_CHUNKS; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue; // only the edge of the current ring
                    }
                    ChunkPos candidate = new ChunkPos(start.x + dx, start.z + dz);
                    if (cheesedimension$isAllPlains(candidate, sampler)) {
                        return candidate;
                    }
                }
            }
        }
        return start; // no land nearby: keep the vanilla position
    }

    @Unique
    private boolean cheesedimension$isAllPlains(ChunkPos chunk, MultiNoiseUtil.MultiNoiseSampler sampler) {
        int cx = chunk.getStartX();
        int cz = chunk.getStartZ();
        int by = BiomeCoords.fromBlock(SAMPLE_Y);
        for (int x = cx - FOOTPRINT_HALF_SIZE; x <= cx + FOOTPRINT_HALF_SIZE; x += SAMPLE_STEP) {
            for (int z = cz - FOOTPRINT_HALF_SIZE; z <= cz + FOOTPRINT_HALF_SIZE; z += SAMPLE_STEP) {
                RegistryEntry<Biome> biome = this.biomeSource.getBiome(
                        BiomeCoords.fromBlock(x), by, BiomeCoords.fromBlock(z), sampler);
                if (!biome.matchesKey(CHEESE_PLAINS)) {
                    return false;
                }
            }
        }
        return true;
    }
}
