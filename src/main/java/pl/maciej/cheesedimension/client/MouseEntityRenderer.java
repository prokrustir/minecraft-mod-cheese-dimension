package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.entity.MouseEntity;

public class MouseEntityRenderer extends MobEntityRenderer<MouseEntity, MouseEntityModel<MouseEntity>> {
	private static final Identifier TEXTURE = CheeseDimensionMod.id("textures/entity/mouse.png");

	public MouseEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new MouseEntityModel<>(context.getPart(MouseEntityModel.LAYER)), 0.2f);
	}

	@Override
	public Identifier getTexture(MouseEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(MouseEntity entity, MatrixStack matrices, float amount) {
		if (entity.isBaby()) {
			matrices.scale(0.5f, 0.5f, 0.5f);
		}
	}
}
