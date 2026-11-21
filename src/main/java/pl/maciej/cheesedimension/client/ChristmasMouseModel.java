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
 * Model layer of the Christmas mice: the standard humanoid (biped) model plus big round ears, a
 * snout, a tall pointed hat whose tip bends to the side, a scarf or collar, a skirt and a tail.
 * Both mice share this model and differ only in their 64x64 texture. The texture uses the biped
 * layout in the top half; the extra parts use the space below it (see the uv coordinates and
 * tools/generate_christmas_mouse_textures.py).
 */
public class ChristmasMouseModel {
	public static final EntityModelLayer LAYER =
			new EntityModelLayer(CheeseDimensionMod.id("christmas_mouse"), "main");

	public static TexturedModelData getTexturedModelData() {
		ModelData data = BipedEntityModel.getModelData(Dilation.NONE, 0.0f);
		ModelPartData root = data.getRoot();

		ModelPartData head = root.getChild("head");
		ModelPartBuilder ear = ModelPartBuilder.create()
				.uv(0, 32).cuboid(-2.5f, -2.5f, -0.5f, 5.0f, 5.0f, 1.0f);
		head.addChild("left_ear", ear, ModelTransform.of(5.5f, -7.0f, 0.0f, 0.0f, 0.0f, 0.3f));
		head.addChild("right_ear", ear, ModelTransform.of(-5.5f, -7.0f, 0.0f, 0.0f, 0.0f, -0.3f));
		head.addChild("snout",
				ModelPartBuilder.create().uv(12, 32).cuboid(-1.5f, 0.0f, -2.0f, 3.0f, 2.0f, 2.0f),
				ModelTransform.pivot(0.0f, -3.0f, -4.0f));

		// Hat: three narrowing parts and a tip; each part sits on top of the previous one and leans
		// a bit more to the side, so the hat looks like a soft, bent elf hat.
		ModelPartData hatBase = head.addChild("hat_base",
				ModelPartBuilder.create().uv(0, 42).cuboid(-4.0f, -3.0f, -4.0f, 8.0f, 3.0f, 8.0f, new Dilation(0.5f)),
				ModelTransform.pivot(0.0f, -8.0f, 0.0f));
		ModelPartData hatMiddle = hatBase.addChild("hat_middle",
				ModelPartBuilder.create().uv(32, 42).cuboid(-2.5f, -3.0f, -2.5f, 5.0f, 3.0f, 5.0f),
				ModelTransform.of(0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.15f));
		ModelPartData hatTop = hatMiddle.addChild("hat_top",
				ModelPartBuilder.create().uv(52, 42).cuboid(-1.5f, -3.0f, -1.5f, 3.0f, 3.0f, 3.0f),
				ModelTransform.of(0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.35f));
		hatTop.addChild("hat_tip",
				ModelPartBuilder.create().uv(56, 48).cuboid(-1.0f, -5.0f, -1.0f, 2.0f, 5.0f, 2.0f),
				ModelTransform.of(0.0f, -3.0f, 0.0f, 0.0f, 0.0f, 0.9f));

		ModelPartData body = root.getChild("body");
		body.addChild("scarf",
				ModelPartBuilder.create().uv(28, 54).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 2.0f, 5.0f),
				ModelTransform.NONE);
		body.addChild("skirt",
				ModelPartBuilder.create().uv(0, 54).cuboid(-4.5f, 0.0f, -2.5f, 9.0f, 5.0f, 5.0f),
				ModelTransform.pivot(0.0f, 8.0f, 0.0f));
		// Negative pitch bends the tail (which points backwards, +z) downwards.
		body.addChild("tail",
				ModelPartBuilder.create().uv(22, 32).cuboid(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 8.0f),
				ModelTransform.of(0.0f, 11.0f, 2.5f, -0.7f, 0.0f, 0.0f));

		return TexturedModelData.of(data, 64, 64);
	}

	private ChristmasMouseModel() {
	}
}
