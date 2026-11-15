package pl.maciej.cheesedimension.client;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.IronGolemEntityRenderer;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.util.Identifier;
import pl.maciej.cheesedimension.CheeseDimensionMod;

/** Cheese Golem: the iron golem model and cracks with a cheese texture. */
public class CheeseGolemEntityRenderer extends IronGolemEntityRenderer {
	private static final Identifier TEXTURE = CheeseDimensionMod.id("textures/entity/cheese_golem.png");

	public CheeseGolemEntityRenderer(EntityRendererFactory.Context context) {
		super(context);
	}

	@Override
	public Identifier getTexture(IronGolemEntity entity) {
		return TEXTURE;
	}
}
