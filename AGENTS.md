# AGENTS.md

Guidance for AI coding agents working in this repository.

## Project

Fabric mod for Minecraft Java Edition **1.21.1**. Adds a cheese dimension, a cheese portal and mice. Mod id: `cheesedimension`. Java package: `pl.maciej.cheesedimension`.

- Mappings: **Yarn** `1.21.1+build.3` (not Mojang mappings). Use Yarn names such as `Identifier`, `ServerWorld`, `BlockPos`.
- Java 21, Gradle (use the wrapper), Fabric Loom, Fabric API for 1.21.1.
- Do not upgrade the Minecraft version, Loom or mappings unless asked. Names and data formats differ between versions.

## Commands

```
./gradlew build        # compile and package; run this before finishing a code change
./gradlew runClient    # launch the game for manual testing (needs a display)
```

There are no automated tests. Verify code changes with `./gradlew build`, and data changes by starting the game and creating a world, because broken worldgen JSON only fails at world load.

## Layout

- `src/main/java/pl/maciej/cheesedimension/`: all code. Registries live in `ModBlocks`, `ModItems`, `ModEntities`, `ModDimensions`. Portal logic is in `portal/CheesePortalHelper` and `block/CheesePortalBlock`.
- `src/main/java/.../client/`: client-only code (model, renderer, render layers). Never reference these classes from server-side code.
- `src/main/resources/assets/cheesedimension/`: models, blockstates, textures, language files (`en_us.json`, `pl_pl.json`).
- `src/main/resources/data/cheesedimension/`: dimension, dimension type, noise settings, biome, recipes, loot tables.

## Conventions

- Register new content in the matching `Mod*` class and call it from `CheeseDimensionMod.onInitialize()`. Order matters: blocks, entities, then items.
- Every new block or item needs: a registry entry, a model (and blockstate for blocks), a texture, and entries in **both** language files. Blocks that drop themselves need a loot table.
- Keep user-facing text in language files, not in code.
- Code comments and identifiers are in English. The README is in English.
- Textures are 16x16 PNGs, except the mouse (32x32). The mouse texture layout must match the UVs in `MouseEntityModel`.

## Minecraft 1.21.1 pitfalls

These formats are specific to 1.21.1 and easy to get wrong:

- Data pack folders are singular: `recipe`, `loot_table`, `tags/block`.
- Recipe results use `"id"` (not `"item"`), and ingredients use `{"item": "..."}`.
- In the biome JSON, `carvers` must be an **object** (`{}`), not a list. `features` is a list of lists.
- A noise router needs all 15 fields, even if they are `0`.
- `EntityType.Builder.build(String)` takes a plain string in this version.
- Attribute names still use the `GENERIC_` prefix (`EntityAttributes.GENERIC_MAX_HEALTH`).
- Item definitions in `assets/<id>/items/` do not exist yet; item models live in `models/item/`.
- The portal block implements `net.minecraft.block.Portal`, and teleporting uses `TeleportTarget` with `SEND_TRAVEL_THROUGH_PORTAL_PACKET` and `ADD_PORTAL_CHUNK_TICKET`.

## Do not commit

`run/`, `build/`, `.gradle/`, IDE folders and logs. They are listed in `.gitignore`. Never edit files in `run/` as part of a change.

## Before finishing a change

1. `./gradlew build` succeeds.
2. New JSON is valid and follows the 1.21.1 formats above.
3. Both language files contain any new keys.
4. README is updated if gameplay or build steps changed.
