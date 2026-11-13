package pl.maciej.cheesedimension.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import pl.maciej.cheesedimension.ModBlocks;
import pl.maciej.cheesedimension.ModEntities;

public class CheeseDimensionClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.CHEESE_PORTAL, RenderLayer.getTranslucent());

		EntityModelLayerRegistry.registerModelLayer(MouseEntityModel.LAYER, MouseEntityModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MOUSE, MouseEntityRenderer::new);
	}
}
