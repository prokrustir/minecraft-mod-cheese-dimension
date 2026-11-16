# Cheese Dimension

A Fabric mod for Minecraft Java Edition 1.21.1 that adds a cheese dimension, a cheese portal to get there, and mice that live in it.

## Features

- **Cheese dimension**: terrain made entirely of cheese, with a surface around y=90 and holes inside like Swiss cheese. It has a normal day and night cycle and no hostile mobs.
- **Cheese portal**: build a frame from cheese blocks (like a Nether portal) and light it with a cheese knife. A return portal is built automatically on the other side if none is found within 8 blocks.
- **Mice**: small animals that only spawn on cheese. They follow players holding cheese and breed when fed cheese.
- **Cheese sauce sea**: the low parts of the cheese dimension (below y=84) are flooded with cheese sauce and form their own biome, "Cheese Sauce Sea". It is a fluid that works like water: it flows, makes new sources, you can swim in it, and you can scoop it with an empty bucket to get a cheese sauce bucket (and place it again).
- **Cheese castle**: a 20x20x20 castle of cheese and moldy cheese blocks (outer wall with a south gate, four corner towers, a two-storey keep, ladders, torches and two loot chests). There is exactly one castle per world (placed like a stronghold, about 100-200 blocks from the world origin of the cheese dimension, on the Cheese Plains) and it is rotated randomly. If the chosen spot is on the cheese sauce sea, the mod moves it to the nearest place where the whole castle fits on the Cheese Plains (searching up to 16 chunks away), so the castle is not lost to the sea. Find it with `/locate structure cheesedimension:cheese_castle` while in the cheese dimension.
- **Cheesehenge**: a ring of tall cheddar pillars with lintels (some broken, like a ruin), an inner horseshoe of five trilithons and a small altar, on a round cheese platform (25x25, up to 8 blocks tall). Several generate in every world on the Cheese Plains, roughly one per 160x160 blocks. Find one with `/locate structure cheesedimension:cheesehenge`.
- **Cheese Golems**: neutral guardians of the Cheesehenge, based on the iron golem: they leave players alone until hurt, attack hostile mobs (including moldy mice) and hit hard. Two stand inside every Cheesehenge from the moment it generates; they do not spawn anywhere else (there is no natural spawning; the spawn egg is for creative use). They never despawn, drop cheese and can be healed by right-clicking with cheese.
- **Rat King**: the ruler of the cheese castle. A friendly, humanoid rat in a red robe and a golden crown who stands on the upper floor of the keep. He does not fight (he is invulnerable and never despawns): right-click him to talk. He tells 8 lines about the cheese dimension, one per click, then repeats from the second line. Dialogue lines are in the language files (`dialogue.cheesedimension.rat_king.N`). Use the Rat King spawn egg to place another one.
- **Moldy cheese**: a whitish cheese with green mold. Eating it poisons you for 10 seconds (it can be eaten at full hunger). Craft it from 1 cheese + 1 fermented spider eye; 4 moldy cheese make a block of moldy cheese, which turns back into 4 moldy cheese. Veins of moldy cheese blocks also generate naturally inside the cheese dimension (y=20-110, both biomes).
- **Moldy mice**: feed a mouse moldy cheese (right click). After 5 seconds it turns into a green moldy mouse that chases the nearest player (also in creative mode, unlike a creeper) and explodes like a creeper (the explosion destroys blocks unless `mobGriefing` is off). This cannot be undone.
- **Cheddar**: a darker, more orange cheese. Get it by cooking cheese in a furnace or smoker; 4 cheddar make a block of cheddar, which turns back into 4 cheddar. The pillars of every Cheesehenge are built from cheddar blocks.
- **Fire mice**: feed a mouse cheddar (right click). After 5 seconds it turns into a red fire mouse that is immune to fire, walks towards the nearest player (also in creative mode) and shoots small fireballs at them, like a blaze. The fireballs set blocks on fire unless `mobGriefing` is off. This cannot be undone.
- **Items and blocks**: cheese, moldy cheese, cheddar, block of cheese, block of moldy cheese, block of cheddar, cheese knife, cheese sauce bucket, mouse spawn egg. All are in the "Cheese Dimension" creative tab.
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
  ModBlocks / ModItems / ModEntities / ModDimensions / ModFluids
  fluid/CheeseSauceFluid       still and flowing cheese sauce
  block/CheesePortalBlock      portal block and teleport logic
  item/CheeseKnifeItem         lights the portal
  portal/CheesePortalHelper    frame detection, portal search and creation
  entity/MouseEntity           mouse mob (turns into a moldy mouse when fed moldy cheese)
  entity/MoldyMouseEntity      exploding green mouse
  entity/FireMouseEntity       red mouse that shoots fireballs
  entity/CheeseGolemEntity     cheese golem (an iron golem with another texture)
  entity/RatKingEntity         the Rat King, a talking NPC (humanoid model in client/RatKingModel)
  client/                      client entrypoint, mouse model and renderer
src/main/resources/
  fabric.mod.json
  assets/cheesedimension/      blockstates, models, textures, lang
  data/cheesedimension/        dimension, dimension_type, worldgen, recipes, loot tables
```

The dimension is fully data-driven. Terrain is defined in `worldgen/noise_settings/cheese.json`. The two biomes (`cheese_plains`, `cheese_sauce_sea`) are chosen by a multi-noise biome source that reads the same noise as the terrain height (`continents` in the noise router), so the sea biome matches the flooded areas.

## Known limitations

- Mouse sounds are borrowed from the rabbit.
- The cheese dimension has no trees or other decoration yet, and the cheese castle is its only structure. The castle is generated by `tools/generate_cheese_castle.py` (it writes `data/cheesedimension/structure/cheese_castle.nbt`); edit the script and run it again to change the layout.
- Portal particles and sounds are not implemented.

## License

MIT, see [LICENSE](LICENSE).
