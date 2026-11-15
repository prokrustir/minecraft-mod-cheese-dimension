package pl.maciej.cheesedimension.client;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import pl.maciej.cheesedimension.CheeseDimensionMod;

/**
 * Model layer of the Rat King: the standard humanoid (biped) model plus rat ears, a snout, a tail,
 * a crown and a robe. The 64x64 texture uses the biped layout in the top half; the extra parts use
 * the free space below it (see the uv coordinates).
 */
public class RatKingModel {
	public static final EntityModelLayer LAYER =
			new EntityModelLayer(CheeseDimensionMod.id("rat_king"), "main");

	public static TexturedModelData getTexturedModelData() {
		ModelData data = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
		ModelPartData root = data.getRoot();

		ModelPartData head = root.getChild("head");
		ModelPartBuilder ear = ModelPartBuilder.create()
				.uv(0, 32).cuboid(-1.5f, -1.5f, -0.5f, 3.0f, 3.0f, 1.0f);
		head.addChild("left_ear", ear, ModelTransform.of(3.8f, -9.0f, 0.0f, 0.0f, 0.0f, 0.35f));
		head.addChild("right_ear", ear, ModelTransform.of(-3.8f, -9.0f, 0.0f, 0.0f, 0.0f, -0.35f));
		head.addChild("snout",
				ModelPartBuilder.create().uv(10, 32).cuboid(-1.5f, 0.0f, -2.0f, 3.0f, 2.0f, 2.0f),
				ModelTransform.pivot(0.0f, -3.0f, -4.0f));
		head.addChild("crown",
				ModelPartBuilder.create().uv(0, 36).cuboid(-3.0f, -2.0f, -3.0f, 6.0f, 2.0f, 6.0f),
				ModelTransform.pivot(0.0f, -8.0f, 0.0f));

		ModelPartData body = root.getChild("body");
		// Negative pitch bends the tail (which points backwards, +z) downwards.
		body.addChild("tail",
				ModelPartBuilder.create().uv(30, 36).cuboid(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 8.0f),
				ModelTransform.of(0.0f, 10.5f, 2.0f, -0.7f, 0.0f, 0.0f));
		body.addChild("robe",
				ModelPartBuilder.create().uv(16, 48).cuboid(-4.0f, 0.0f, -2.0f, 8.0f, 12.0f, 4.0f, new Dilation(0.35f)),
				ModelTransform.NONE);

		return TexturedModelData.of(data, 64, 64);
	}

	private RatKingModel() {
	}
}
