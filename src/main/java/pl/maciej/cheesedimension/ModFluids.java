package pl.maciej.cheesedimension;

import net.minecraft.fluid.FlowableFluid;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import pl.maciej.cheesedimension.fluid.CheeseSauceFluid;

public class ModFluids {
	public static final FlowableFluid STILL_CHEESE_SAUCE = Registry.register(Registries.FLUID,
			CheeseDimensionMod.id("cheese_sauce"), new CheeseSauceFluid.Still());
	public static final FlowableFluid FLOWING_CHEESE_SAUCE = Registry.register(Registries.FLUID,
			CheeseDimensionMod.id("flowing_cheese_sauce"), new CheeseSauceFluid.Flowing());

	public static void register() {
		// Calling this loads the class and its static fields.
	}

	private ModFluids() {
	}
}
