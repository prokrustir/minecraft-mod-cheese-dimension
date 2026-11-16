package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.entity.FireMouseEntity;

/** Red mouse: same model as the mouse, always fully lit (it glows like a blaze). */
public class FireMouseEntityRenderer extends MobEntityRenderer<FireMouseEntity, MouseEntityModel<FireMouseEntity>> {
	private static final Identifier TEXTURE = CheeseDimensionMod.id("textures/entity/fire_mouse.png");

	public FireMouseEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new MouseEntityModel<>(context.getPart(MouseEntityModel.LAYER)), 0.2f);
	}

	@Override
	public Identifier getTexture(FireMouseEntity entity) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLight(FireMouseEntity entity, BlockPos pos) {
		return 15;
	}
}
