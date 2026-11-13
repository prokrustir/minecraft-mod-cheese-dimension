package pl.maciej.cheesedimension;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public class ModDimensions {
	/** Klucz swiata odpowiada plikowi data/cheesedimension/dimension/cheese.json */
	public static final RegistryKey<World> CHEESE_WORLD =
			RegistryKey.of(RegistryKeys.WORLD, CheeseDimensionMod.id("cheese"));

	private ModDimensions() {
	}
}
