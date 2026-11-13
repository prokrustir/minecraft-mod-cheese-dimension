package pl.maciej.cheesedimension.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.ModEntities;
import pl.maciej.cheesedimension.ModFluids;

public class CheeseDimensionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CHEESE_PORTAL, RenderLayer.getTranslucent());

		FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.STILL_CHEESE_SAUCE, ModFluids.FLOWING_CHEESE_SAUCE,
				new SimpleFluidRenderHandler(
						CheeseDimensionMod.id("block/cheese_sauce_still"),
						CheeseDimensionMod.id("block/cheese_sauce_flow")));
		BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
				ModFluids.STILL_CHEESE_SAUCE, ModFluids.FLOWING_CHEESE_SAUCE);

		EntityModelLayerRegistry.registerModelLayer(MouseEntityModel.LAYER, MouseEntityModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MOUSE, MouseEntityRenderer::new);
	}
}
