package pl.maciej.cheesedimension;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocationTypes;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.world.Heightmap;
import pl.maciej.cheesedimension.entity.MoldyMouseEntity;
import pl.maciej.cheesedimension.entity.MouseEntity;

public class ModEntities {
	public static final EntityType<MouseEntity> MOUSE = Registry.register(
			Registries.ENTITY_TYPE,
			CheeseDimensionMod.id("mouse"),
			EntityType.Builder.create(MouseEntity::new, SpawnGroup.CREATURE)
					.dimensions(0.4f, 0.3f)
					.maxTrackingRange(8)
					.build("mouse"));

	public static final EntityType<MoldyMouseEntity> MOLDY_MOUSE = Registry.register(
			Registries.ENTITY_TYPE,
			CheeseDimensionMod.id("moldy_mouse"),
			EntityType.Builder.create(MoldyMouseEntity::new, SpawnGroup.MONSTER)
					.dimensions(0.4f, 0.3f)
					.maxTrackingRange(8)
					.build("moldy_mouse"));

	public static void register() {
		FabricDefaultAttributeRegistry.register(MOUSE, MouseEntity.createMouseAttributes());
		FabricDefaultAttributeRegistry.register(MOLDY_MOUSE, MoldyMouseEntity.createMoldyMouseAttributes());
		SpawnRestriction.register(
				MOUSE,
				SpawnLocationTypes.ON_GROUND,
				Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,
				MouseEntity::canSpawnOnCheese);
	}

	private ModEntities() {
	}
}
