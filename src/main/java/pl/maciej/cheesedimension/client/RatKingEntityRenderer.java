package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.entity.RatKingEntity;

/** The Rat King is the mouse model blown up 2.5 times with a darker texture and a golden crown. */
public class RatKingEntityRenderer extends MobEntityRenderer<RatKingEntity, MouseEntityModel<RatKingEntity>> {
	private static final Identifier TEXTURE = CheeseDimensionMod.id("textures/entity/rat_king.png");
	private static final float SCALE = 2.5f;

	public RatKingEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new MouseEntityModel<>(context.getPart(MouseEntityModel.LAYER)), 0.5f);
	}

	@Override
	public Identifier getTexture(RatKingEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(RatKingEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(SCALE, SCALE, SCALE);
	}
}
