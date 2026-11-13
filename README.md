# Cheese Dimension

A Fabric mod for Minecraft Java Edition 1.21.1 that adds a cheese dimension, a cheese portal to get there, and mice that live in it.

## Features

- **Cheese dimension**: terrain made entirely of cheese, with a surface around y=90 and holes inside like Swiss cheese. It has a normal day and night cycle and no hostile mobs.
- **Cheese portal**: build a frame from cheese blocks (like a Nether portal) and light it with a cheese knife. A return portal is built automatically on the other side if none is found within 8 blocks.
- **Mice**: small animals that only spawn on cheese. They follow players holding cheese and breed when fed cheese.
- **Items and blocks**: cheese, block of cheese, cheese knife, mouse spawn egg. All are in the "Cheese Dimension" creative tab.
- **Languages**: English and Polish.

## Requirements

- JDK 21
- Minecraft 1.21.1 with [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api) (to play)
- Internet access on the first build (Gradle downloads Minecraft and libraries)

## Build and run

```
./gradlew runClient    # start the game with the mod for testing
./gradlew build        # build the mod, output in build/libs/
```

On Windows use `gradlew.bat`. To install the mod, copy `build/libs/cheese-dimension-<version>.jar` (not the `-sources` jar) into your `mods` folder together with Fabric API.

If Gradle complains about versions, check https://fabricmc.net/develop and update `loader_version` and `fabric_version` in `gradle.properties` and the Loom version in `build.gradle`.

## How to play

1. Get cheese. Put a milk bucket in the crafting grid to get 4 cheese (the bucket is returned). Cows give milk.
2. Craft a block of cheese from 4 cheese (2x2). A block can be turned back into 4 cheese.
3. Craft a cheese knife: iron ingot on top, cheese in the middle, stick at the bottom.
4. Build a portal frame from cheese blocks, standing upright. The interior must be at least 2 wide and 3 tall (up to 21x21), and the bottom row of the frame must be cheese. Corners are optional.
   ```
   . C C .
   C . . C
   C . . C
   C . . C
   . C C .
   ```
5. Right-click a frame block from the inside with the cheese knife. The interior fills with the portal. Lighting works only in the Overworld and in the cheese dimension.
6. Step in and wait a few seconds (instant in creative mode).

## Project layout

```
src/main/java/pl/maciej/cheesedimension/
  CheeseDimensionMod.java      main entrypoint
  ModBlocks / ModItems / ModEntities / ModDimensions
  block/CheesePortalBlock      portal block and teleport logic
  item/CheeseKnifeItem         lights the portal
  portal/CheesePortalHelper    frame detection, portal search and creation
  entity/MouseEntity           mouse mob
  client/                      client entrypoint, mouse model and renderer
src/main/resources/
  fabric.mod.json
  assets/cheesedimension/      blockstates, models, textures, lang
  data/cheesedimension/        dimension, dimension_type, worldgen, recipes, loot tables
```

The dimension is fully data-driven. Terrain is defined in `worldgen/noise_settings/cheese.json` and the only biome in `worldgen/biome/cheese_plains.json`.

## Known limitations

- Mouse sounds are borrowed from the rabbit.
- The cheese dimension has no structures, trees or other decoration yet.
- Portal particles and sounds are not implemented.

## License

MIT, see [LICENSE](LICENSE).
