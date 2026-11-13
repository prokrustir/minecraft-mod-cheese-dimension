package pl.maciej.cheesedimension;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import pl.maciej.cheesedimension.block.CheesePortalBlock;

public class ModBlocks {
	public static final Block CHEESE_BLOCK = registerWithItem("cheese_block",
			new Block(AbstractBlock.Settings.create()
					.mapColor(MapColor.YELLOW)
					.strength(0.5f)
					.sounds(BlockSoundGroup.SPONGE)));

	public static final Block CHEESE_PORTAL = register("cheese_portal",
			new CheesePortalBlock(AbstractBlock.Settings.create()
					.mapColor(MapColor.YELLOW)
					.noCollision()
					.strength(-1.0f)
					.sounds(BlockSoundGroup.GLASS)
					.luminance(state -> 11)
					.dropsNothing()
					.pistonBehavior(PistonBehavior.BLOCK)));

	private static Block register(String name, Block block) {
		return Registry.register(Registries.BLOCK, CheeseDimensionMod.id(name), block);
	}

	private static Block registerWithItem(String name, Block block) {
		Registry.register(Registries.BLOCK, CheeseDimensionMod.id(name), block);
		Registry.register(Registries.ITEM, CheeseDimensionMod.id(name), new BlockItem(block, new Item.Settings()));
		return block;
	}

	public static void register() {
		// Wywolanie laduje klase, a z nia statyczne pola.
	}

	private ModBlocks() {
	}
}
