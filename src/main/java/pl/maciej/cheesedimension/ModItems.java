package pl.maciej.cheesedimension;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import pl.maciej.cheesedimension.item.CheeseKnifeItem;

public class ModItems {
	public static final Item CHEESE = register("cheese",
			new Item(new Item.Settings().food(new FoodComponent.Builder()
					.nutrition(4)
					.saturationModifier(0.6f)
					.build())));

	public static final Item MOLDY_CHEESE = register("moldy_cheese",
			new Item(new Item.Settings().food(new FoodComponent.Builder()
					.nutrition(3)
					.saturationModifier(0.3f)
					.alwaysEdible()
					.statusEffect(new StatusEffectInstance(StatusEffects.POISON, 20 * 10), 1.0f)
					.build())));

	public static final Item CHEESE_KNIFE = register("cheese_knife",
			new CheeseKnifeItem(new Item.Settings().maxDamage(64)));

	public static final Item CHEESE_SAUCE_BUCKET = register("cheese_sauce_bucket",
			new BucketItem(ModFluids.STILL_CHEESE_SAUCE,
					new Item.Settings().recipeRemainder(Items.BUCKET).maxCount(1)));

	public static final Item MOUSE_SPAWN_EGG = register("mouse_spawn_egg",
			new SpawnEggItem(ModEntities.MOUSE, 0x9E9E9E, 0xF4B6C2, new Item.Settings()));

	public static final RegistryKey<ItemGroup> GROUP_KEY =
			RegistryKey.of(RegistryKeys.ITEM_GROUP, CheeseDimensionMod.id("main"));

	private static Item register(String name, Item item) {
		return Registry.register(Registries.ITEM, CheeseDimensionMod.id(name), item);
	}

	public static void register() {
		ItemGroup group = FabricItemGroup.builder()
				.icon(() -> new ItemStack(CHEESE))
				.displayName(Text.translatable("itemGroup.cheesedimension.main"))
				.entries((context, entries) -> {
					entries.add(ModBlocks.CHEESE_BLOCK);
					entries.add(ModBlocks.MOLDY_CHEESE_BLOCK);
					entries.add(CHEESE);
					entries.add(MOLDY_CHEESE);
					entries.add(CHEESE_KNIFE);
					entries.add(CHEESE_SAUCE_BUCKET);
					entries.add(MOUSE_SPAWN_EGG);
				})
				.build();
		Registry.register(Registries.ITEM_GROUP, GROUP_KEY, group);
	}

	private ModItems() {
	}
}
