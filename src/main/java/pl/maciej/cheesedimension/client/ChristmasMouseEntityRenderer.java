package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import pl.maciej.cheesedimension.entity.ChristmasMouseEntity;

/**
 * Renders a Christmas mouse with the humanoid model from {@link ChristmasMouseModel}, a bit smaller
 * than a player. The texture is passed in, so the same renderer serves both Christmas mice.
 */
public class ChristmasMouseEntityRenderer
		extends MobEntityRenderer<ChristmasMouseEntity, BipedEntityModel<ChristmasMouseEntity>> {
	private static final float SCALE = 0.8f;

	private final Identifier texture;

	public ChristmasMouseEntityRenderer(EntityRendererFactory.Context context, Identifier texture) {
		super(context, new BipedEntityModel<>(context.getPart(ChristmasMouseModel.LAYER)), 0.4f);
		this.texture = texture;
	}

	@Override
	public Identifier getTexture(ChristmasMouseEntity entity) {
		return texture;
	}

	@Override
	protected void scale(ChristmasMouseEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(SCALE, SCALE, SCALE);
	}
}
