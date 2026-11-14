package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import pl.maciej.cheesedimension.CheeseDimensionMod;
import pl.maciej.cheesedimension.entity.MoldyMouseEntity;

/** Green mouse: same model as the mouse, swells before exploding like a creeper. */
public class MoldyMouseEntityRenderer extends MobEntityRenderer<MoldyMouseEntity, MouseEntityModel<MoldyMouseEntity>> {
	private static final Identifier TEXTURE = CheeseDimensionMod.id("textures/entity/moldy_mouse.png");

	public MoldyMouseEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new MouseEntityModel<>(context.getPart(MouseEntityModel.LAYER)), 0.2f);
	}

	@Override
	public Identifier getTexture(MoldyMouseEntity entity) {
		return TEXTURE;
	}

	@Override
	protected void scale(MoldyMouseEntity entity, MatrixStack matrices, float amount) {
		float swell = entity.getClientFuseTime(amount);
		float wobble = 1.0f + MathHelper.sin(swell * 100.0f) * swell * 0.01f;
		swell = MathHelper.clamp(swell, 0.0f, 1.0f);
		swell *= swell;
		swell *= swell;
		float horizontal = (1.0f + swell * 0.4f) * wobble;
		float vertical = (1.0f + swell * 0.1f) / wobble;
		matrices.scale(horizontal, vertical, horizontal);
	}
}
