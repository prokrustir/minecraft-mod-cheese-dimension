#!/usr/bin/env python3
"""Generates the cheese castle structure template (20x20x20) as a Minecraft structure .nbt file.

Usage: python3 tools/generate_cheese_castle.py src/main/resources/data/cheesedimension/structure/cheese_castle.nbt

Edit the layout below and run the script again; the .nbt is a binary file that cannot be reviewed by hand.
Axes: x = east, z = south, y = up. The gate is in the south wall (z = 19); the whole structure is
rotated randomly when it is generated in the world.
"""
import random
import sys

import nbt

N = 20


# Block palette: key -> (block name, properties)
PALETTE = {
    "air": ("minecraft:air", {}),
    "cheese": ("cheesedimension:cheese_block", {}),
    "moldy": ("cheesedimension:moldy_cheese_block", {}),
    "torch": ("minecraft:torch", {}),
    "chest_west": ("minecraft:chest", {"facing": "west", "type": "single", "waterlogged": "false"}),
    "chest_east": ("minecraft:chest", {"facing": "east", "type": "single", "waterlogged": "false"}),
    "ladder_east": ("minecraft:ladder", {"facing": "east", "waterlogged": "false"}),
    "ladder_west": ("minecraft:ladder", {"facing": "west", "waterlogged": "false"}),
    "ladder_south": ("minecraft:ladder", {"facing": "south", "waterlogged": "false"}),
}
CHEST_LOOT = "cheesedimension:chests/cheese_castle"

# Entities placed with the template: the Rat King stands in the middle of the keep's upper floor
# (floor slab at y 9, so the standing level is y 10.0).
RAT_KING_POS = (9.5, 10.0, 9.5)

rnd = random.Random(2024)
grid = {}


def put(x, y, z, key):
    grid[(x, y, z)] = key


def cheese():
    """Mostly cheese with some moldy blocks mixed in, so the castle looks white and green."""
    return "moldy" if rnd.random() < 0.12 else "cheese"


def build():
    # Foundation (y 0-1, sinks into the ground) and courtyard floor (y 2).
    for y in range(0, 3):
        for x in range(N):
            for z in range(N):
                put(x, y, z, cheese())

    # Outer wall: ring 2 blocks thick, y 3-9, walkway on top (standing level y 10), parapet at y 10.
    for x in range(N):
        for z in range(N):
            edge = min(x, z, N - 1 - x, N - 1 - z)
            if edge < 2:
                for y in range(3, 10):
                    put(x, y, z, cheese())
                if edge == 0 and (x + z) % 2 == 0:
                    put(x, 10, z, cheese())

    # Corner towers 5x5: hollow, floor at y 9, roof at y 16, battlements at y 17.
    for ox, oz in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        for lx in range(5):
            for lz in range(5):
                x, z = ox + lx, oz + lz
                perimeter = lx in (0, 4) or lz in (0, 4)
                for y in range(3, 16):
                    if y == 9:
                        put(x, y, z, cheese())
                    else:
                        put(x, y, z, cheese() if perimeter else "air")
                put(x, 16, z, "moldy")
                if perimeter and (x + z) % 2 == 0:
                    put(x, 17, z, cheese())

    # Keep 8x8 in the middle: two floors (floor at y 9), roof at y 18, battlements at y 19.
    for x in range(6, 14):
        for z in range(6, 14):
            perimeter = x in (6, 13) or z in (6, 13)
            for y in range(3, 18):
                if y == 9:
                    put(x, y, z, cheese())
                else:
                    put(x, y, z, cheese() if perimeter else "air")
            put(x, 18, z, "moldy")
            if perimeter and (x + z) % 2 == 0:
                put(x, 19, z, cheese())

    carve()
    furnish()


def carve():
    # Main gate in the south wall.
    for x in range(8, 12):
        for z in (18, 19):
            for y in range(3, 7):
                put(x, y, z, "air")
    # Keep door (south face) and keep windows.
    for x in (9, 10):
        for y in range(3, 6):
            put(x, y, 13, "air")
    for y in (12, 13):
        for c in (8, 11):
            put(c, y, 6, "air")
            put(c, y, 13, "air")
            put(6, y, c, "air")
            put(13, y, c, "air")
    for ox, oz in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        west = ox == 0
        north = oz == 0
        inner_x = ox + 4 if west else ox        # tower face that looks at the courtyard
        courtyard_z = oz + 3 if north else oz + 1   # row of that face that is not covered by the wall ring
        walkway_z = oz + 1 if north else oz + 3     # row of that face that touches the wall ring
        # Door from the courtyard into the tower.
        for y in (3, 4):
            put(inner_x, y, courtyard_z, "air")
        # Opening from the tower onto the wall walkway.
        for y in (10, 11):
            put(inner_x, y, walkway_z, "air")
        # Windows on the outer faces (not on the ladder row oz + 2, the ladder is attached there).
        outer_x = ox if west else ox + 4
        outer_z = oz if north else oz + 4
        for y in (12, 13):
            put(outer_x, y, oz + 1, "air")
            put(ox + 2, y, outer_z, "air")
        # Ladder hole in the tower floor.
        put(ox + 1 if west else ox + 3, 9, oz + 2, "air")
    # Keep ladder holes (ground -> upper floor, upper floor -> roof).
    put(9, 9, 7, "air")
    put(11, 18, 7, "air")


def furnish():
    # Tower ladders (attached to the outer tower wall) up to the walkway level.
    for ox, oz in [(0, 0), (15, 0), (0, 15), (15, 15)]:
        west = ox == 0
        x = ox + 1 if west else ox + 3
        z = oz + 2
        key = "ladder_east" if west else "ladder_west"
        for y in range(3, 11):
            put(x, y, z, key)
    # Keep ladders on the inner north wall.
    for y in range(3, 11):
        put(9, y, 7, "ladder_south")
    for y in range(10, 19):
        put(11, y, 7, "ladder_south")

    # Torches on the floors.
    for pos in [(2, 3, 2), (17, 3, 2), (2, 3, 17), (17, 3, 17), (7, 3, 17), (12, 3, 17),
                (7, 3, 7), (12, 3, 7), (7, 3, 12),
                (7, 10, 7), (12, 10, 7), (12, 10, 12)]:
        put(*pos, "torch")

    # Loot chests: ground floor and upper floor of the keep.
    put(12, 3, 12, "chest_west")
    put(7, 10, 12, "chest_east")


def write_nbt(path):
    keys = list(PALETTE)
    palette = []
    for key in keys:
        name, props = PALETTE[key]
        entry = {"Name": ("string", name)}
        if props:
            entry["Properties"] = ("compound", {k: ("string", v) for k, v in props.items()})
        palette.append(entry)

    blocks = []
    for x in range(N):
        for y in range(N):
            for z in range(N):
                key = grid.get((x, y, z), "air")
                block = {"pos": ("list", ("int", [x, y, z])), "state": ("int", keys.index(key))}
                if key.startswith("chest"):
                    block["nbt"] = ("compound", {
                        "id": ("string", "minecraft:chest"),
                        "LootTable": ("string", CHEST_LOOT),
                    })
                blocks.append(block)

    king = {
        "pos": ("list", ("double", list(RAT_KING_POS))),
        "blockPos": ("list", ("int", [int(c) for c in RAT_KING_POS])),
        "nbt": ("compound", {
            "id": ("string", "cheesedimension:rat_king"),
            "PersistenceRequired": ("byte", 1),
        }),
    }

    root = {
        "DataVersion": ("int", nbt.DATA_VERSION),
        "size": ("list", ("int", [N, N, N])),
        "palette": ("list", ("compound", palette)),
        "blocks": ("list", ("compound", blocks)),
        "entities": ("list", ("compound", [king])),
    }
    nbt.write_gzipped(path, root)


def preview():
    symbols = {"air": ".", "cheese": "#", "moldy": "m", "torch": "t", "chest_west": "C", "chest_east": "C",
               "ladder_east": "H", "ladder_west": "H", "ladder_south": "H"}
    for y in (3, 10, 17):
        print(f"--- layer y={y} (north at top) ---")
        for z in range(N):
            print("".join(symbols[grid.get((x, y, z), "air")] for x in range(N)))


if __name__ == "__main__":
    build()
    if len(sys.argv) > 2 and sys.argv[2] == "--preview":
        preview()
    write_nbt(sys.argv[1])
    print("wrote", sys.argv[1], "with", len(grid), "non-air cells")
