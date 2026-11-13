package pl.maciej.cheesedimension.client;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.util.math.MathHelper;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.entity.MouseEntity;

/** Prosty model myszy: tulow, glowa z uszami, ogon i cztery lapki. Tekstura 32x32. */
public class MouseEntityModel extends SinglePartEntityModel<MouseEntity> {
	public static final EntityModelLayer LAYER =
			new EntityModelLayer(CheeseDimensionMod.id("mouse"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart backRightLeg;

	public MouseEntityModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.tail = root.getChild("tail");
		this.frontLeftLeg = root.getChild("front_left_leg");
		this.frontRightLeg = root.getChild("front_right_leg");
		this.backLeftLeg = root.getChild("back_left_leg");
		this.backRightLeg = root.getChild("back_right_leg");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData data = new ModelData();
		ModelPartData root = data.getRoot();

		root.addChild("body",
				ModelPartBuilder.create().uv(0, 0).cuboid(-2.5f, -4.0f, -4.0f, 5.0f, 4.0f, 8.0f),
				ModelTransform.pivot(0.0f, 22.0f, 0.0f));

		ModelPartData head = root.addChild("head",
				ModelPartBuilder.create()
						.uv(0, 12).cuboid(-2.0f, -2.0f, -3.0f, 4.0f, 4.0f, 3.0f)
						.uv(14, 12).cuboid(-0.5f, -0.5f, -4.0f, 1.0f, 1.0f, 1.0f),
				ModelTransform.pivot(0.0f, 20.0f, -4.0f));

		head.addChild("left_ear",
				ModelPartBuilder.create().uv(0, 20).cuboid(-1.0f, -3.0f, 0.0f, 2.0f, 3.0f, 1.0f),
				ModelTransform.pivot(1.5f, -1.5f, -1.5f));
		head.addChild("right_ear",
				ModelPartBuilder.create().uv(0, 20).cuboid(-1.0f, -3.0f, 0.0f, 2.0f, 3.0f, 1.0f),
				ModelTransform.pivot(-1.5f, -1.5f, -1.5f));

		root.addChild("tail",
				ModelPartBuilder.create().uv(12, 20).cuboid(-0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 6.0f),
				ModelTransform.of(0.0f, 20.5f, 4.0f, 0.4f, 0.0f, 0.0f));

		ModelPartBuilder leg = ModelPartBuilder.create().uv(0, 26).cuboid(-0.5f, 0.0f, -0.5f, 1.0f, 2.0f, 1.0f);
		root.addChild("front_left_leg", leg, ModelTransform.pivot(1.8f, 22.0f, -2.8f));
		root.addChild("front_right_leg", leg, ModelTransform.pivot(-1.8f, 22.0f, -2.8f));
		root.addChild("back_left_leg", leg, ModelTransform.pivot(1.8f, 22.0f, 2.8f));
		root.addChild("back_right_leg", leg, ModelTransform.pivot(-1.8f, 22.0f, 2.8f));

		return TexturedModelData.of(data, 32, 32);
	}

	@Override
	public void setAngles(MouseEntity entity, float limbAngle, float limbDistance, float animationProgress,
						  float headYaw, float headPitch) {
		this.getPart().traverse().forEach(ModelPart::resetTransform);

		this.head.yaw = headYaw * MathHelper.RADIANS_PER_DEGREE;
		this.head.pitch = headPitch * MathHelper.RADIANS_PER_DEGREE;

		float swing = MathHelper.cos(limbAngle * 1.3f) * 1.2f * limbDistance;
		this.frontLeftLeg.pitch = swing;
		this.backRightLeg.pitch = swing;
		this.frontRightLeg.pitch = -swing;
		this.backLeftLeg.pitch = -swing;

		this.tail.yaw = MathHelper.sin(animationProgress * 0.2f) * 0.3f + MathHelper.cos(limbAngle * 1.3f) * 0.4f * limbDistance;
	}

	@Override
	public ModelPart getPart() {
		return this.root;
	}
}
