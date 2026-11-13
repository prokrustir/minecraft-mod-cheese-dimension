package pl.maciej.cheesedimension;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

public class CheeseDimensionMod implements ModInitializer {
	public static final String MOD_ID = "cheesedimension";

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		// Kolejnosc ma znaczenie: blok sera i jedzenie sa potrzebne myszy i jajku.
		ModBlocks.register();
		ModEntities.register();
		ModItems.register();
	}
}
