package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.util.Identifier;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.entity.RatKingEntity;

/** Renders the Rat King with the humanoid model from {@link RatKingModel}. */
public class RatKingEntityRenderer extends MobEntityRenderer<RatKingEntity, BipedEntityModel<RatKingEntity>> {
	private static final Identifier TEXTURE = CheeseDimensionMod.id("textures/entity/rat_king.png");

	public RatKingEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new BipedEntityModel<>(context.getPart(RatKingModel.LAYER)), 0.5f);
	}

	@Override
	public Identifier getTexture(RatKingEntity entity) {
		return TEXTURE;
	}
}
