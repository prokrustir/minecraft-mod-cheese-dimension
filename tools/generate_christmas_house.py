#!/usr/bin/env python3
"""Generates the Christmas House structure template (15 x 13 x 13) as a Minecraft structure .nbt file.

Usage: python3 tools/generate_christmas_house.py src/main/resources/data/cheesedimension/structure/christmas_house.nbt [--preview]

A wooden house (spruce planks, spruce log corners, cobblestone foundation) with a gable roof of
spruce stairs and a snowy ridge. Inside: a brick fireplace with a campfire and a chimney, a
decorated Christmas tree with presents, a table with a cake, a rug, a loot chest and the two
Christmas mice who live there. The door is in the south wall.

Axes: x = east, z = south, y = up. The house walls are x 1-13, z 1-11; x 0/14 and z 0/12 hold only
the roof overhang. The structure is rotated randomly when it is generated in the world.
"""
import sys

import nbt

SX, SY, SZ = 15, 13, 13
WALL_X0, WALL_X1 = 1, 13
WALL_Z0, WALL_Z1 = 1, 11
ROOF_Y = 5          # lowest roof row (also the top plate of the walls)
RIDGE_Z = 6

CHEST_LOOT = "cheesedimension:chests/christmas_house"
# Entities placed with the template: (entity id, x, y, z), standing on the floor (y 1.0).
MICE = [("cheesedimension:elf_mouse", 4.5, 1.0, 8.5), ("cheesedimension:christmas_mouse", 9.5, 1.0, 8.5)]

palette = []        # list of (name, props)
palette_index = {}
grid = {}


def state(name, **props):
    key = (name, tuple(sorted(props.items())))
    if key not in palette_index:
        palette_index[key] = len(palette)
        palette.append((name, {k: str(v).lower() for k, v in props.items()}))
    return palette_index[key]


def put(x, y, z, name, **props):
    assert 0 <= x < SX and 0 <= y < SY and 0 <= z < SZ, (x, y, z)
    grid[(x, y, z)] = state("minecraft:" + name if ":" not in name else name, **props)


def roof_height(z):
    """Height of the roof row above column z (the stairs block itself)."""
    return ROOF_Y + min(z, SZ - 1 - z)


def leaves():
    return dict(distance=1, persistent=True, waterlogged=False)


def build():
    # Everything starts as structure void, so the terrain around the house stays as it is.
    for x in range(SX):
        for y in range(SY):
            for z in range(SZ):
                put(x, y, z, "structure_void")

    # Foundation, floor, walls and the air inside.
    for x in range(WALL_X0, WALL_X1 + 1):
        for z in range(WALL_Z0, WALL_Z1 + 1):
            edge_x = x in (WALL_X0, WALL_X1)
            edge_z = z in (WALL_Z0, WALL_Z1)
            put(x, 0, z, "cobblestone" if edge_x or edge_z else "spruce_planks")
            for y in range(1, roof_height(z)):
                if edge_x and edge_z:
                    put(x, y, z, "spruce_log", axis="y")
                elif y == ROOF_Y and edge_z:
                    put(x, y, z, "spruce_log", axis="x")      # top plate of the long walls
                elif y == ROOF_Y and edge_x:
                    put(x, y, z, "spruce_log", axis="z")      # top plate of the gable walls
                elif edge_z and y < ROOF_Y:
                    put(x, y, z, "spruce_planks")
                elif edge_x:
                    put(x, y, z, "spruce_planks")             # gable walls go up to the roof
                else:
                    put(x, y, z, "air")

    # Roof: spruce stairs on both slopes, a ridge of snow, overhanging by one block on every side.
    for x in range(SX):
        for z in range(SZ):
            y = roof_height(z)
            if z < RIDGE_Z:
                put(x, y, z, "spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
            elif z > RIDGE_Z:
                put(x, y, z, "spruce_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
            else:
                put(x, y, z, "snow_block")

    windows_and_door()
    fireplace()
    christmas_tree()
    furniture()


def windows_and_door():
    pane_x = dict(east=True, west=True, north=False, south=False, waterlogged=False)
    pane_z = dict(east=False, west=False, north=True, south=True, waterlogged=False)
    for y in (2, 3):
        for x in (3, 4, 10, 11):                 # south wall, both sides of the door
            put(x, y, WALL_Z1, "glass_pane", **pane_x)
        for x in (5, 6):                         # north wall
            put(x, y, WALL_Z0, "glass_pane", **pane_x)
        for z in (5, 6, 7):                      # east wall
            put(WALL_X1, y, z, "glass_pane", **pane_z)
    for x in (WALL_X0, WALL_X1):                 # small round-ish window high in both gables
        put(x, 7, RIDGE_Z, "glass_pane", **pane_z)
    # Door in the middle of the south wall, with a torch on each side outside.
    door = dict(facing="north", hinge="left", open=False, powered=False)
    put(7, 1, WALL_Z1, "spruce_door", half="lower", **door)
    put(7, 2, WALL_Z1, "spruce_door", half="upper", **door)
    for x in (6, 8):
        put(x, 3, WALL_Z1 + 1, "wall_torch", facing="south")


def fireplace():
    # Brick fireplace against the west wall with a campfire, and a chimney above the roof.
    x = WALL_X0 + 1
    for y in range(1, 5):
        put(x, y, RIDGE_Z - 1, "bricks")
        put(x, y, RIDGE_Z + 1, "bricks")
    put(x, 3, RIDGE_Z, "bricks")
    put(x, 4, RIDGE_Z, "bricks")
    put(x, 1, RIDGE_Z, "campfire", facing="east", lit=True, signal_fire=False, waterlogged=False)
    put(x, 2, RIDGE_Z, "air")
    for y in range(1, SY):
        put(WALL_X0, y, RIDGE_Z, "bricks")       # back of the fireplace and the chimney


def christmas_tree():
    cx, cz = 10, 4
    layers = {2: 2, 3: 2, 4: 1, 5: 1, 6: 0}      # y -> radius of the leaf diamond
    for y, r in layers.items():
        for dx in range(-r, r + 1):
            for dz in range(-r, r + 1):
                if abs(dx) + abs(dz) <= r:
                    put(cx + dx, y, cz + dz, "spruce_leaves", **leaves())
    for y in range(1, 6):
        put(cx, y, cz, "spruce_log", axis="y")
    # Baubles (red and gold) and lights (shroomlight) on the outside of the tree, a star on top.
    for (dx, y, dz), block in {(-2, 2, 0): "red_wool", (2, 2, 0): "gold_block", (0, 2, 2): "shroomlight",
                               (1, 3, 1): "red_wool", (-1, 3, -1): "shroomlight", (0, 3, 2): "gold_block",
                               (-1, 4, 0): "red_wool", (0, 4, 1): "shroomlight", (1, 5, 0): "gold_block",
                               (0, 5, -1): "red_wool"}.items():
        put(cx + dx, y, cz + dz, block)
    put(cx, 7, cz, "glowstone")
    # Presents under the tree.
    for (dx, dz), block in {(-1, 1): "red_wool", (1, 1): "lime_wool", (-1, -1): "light_blue_wool",
                            (2, 1): "yellow_wool"}.items():
        put(cx + dx, 1, cz + dz, block)


def furniture():
    # Table with a cake and a lantern, two chairs.
    for x in (5, 6):
        put(x, 1, 6, "stripped_spruce_log", axis="x")
    put(5, 2, 6, "cake", bites=0)
    put(6, 2, 6, "lantern", hanging=False, waterlogged=False)
    put(5, 1, 5, "spruce_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
    put(6, 1, 7, "spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    # Red rug in front of the fireplace and the door.
    for x in range(3, 11):
        for z in (8, 9):
            put(x, 1, z, "red_carpet")
    # Loot chest and some lanterns in the corners.
    put(12, 1, 9, "chest", facing="west", type="single", waterlogged=False)
    put(12, 1, 10, "lantern", hanging=False, waterlogged=False)
    put(2, 1, 10, "lantern", hanging=False, waterlogged=False)
    put(2, 1, 2, "lantern", hanging=False, waterlogged=False)


def write_nbt(path):
    blocks = []
    chest = state("minecraft:chest", facing="west", type="single", waterlogged=False)
    for x in range(SX):
        for y in range(SY):
            for z in range(SZ):
                s = grid[(x, y, z)]
                block = {"pos": ("list", ("int", [x, y, z])), "state": ("int", s)}
                if s == chest:
                    block["nbt"] = ("compound", {
                        "id": ("string", "minecraft:chest"),
                        "LootTable": ("string", CHEST_LOOT),
                    })
                blocks.append(block)
    mice = [{
        "pos": ("list", ("double", [mx, my, mz])),
        "blockPos": ("list", ("int", [int(mx), int(my), int(mz)])),
        "nbt": ("compound", {"id": ("string", entity), "PersistenceRequired": ("byte", 1)}),
    } for entity, mx, my, mz in MICE]
    root = {
        "DataVersion": ("int", nbt.DATA_VERSION),
        "size": ("list", ("int", [SX, SY, SZ])),
        "palette": ("list", ("compound", [nbt.palette_entry(name, props) for name, props in palette])),
        "blocks": ("list", ("compound", blocks)),
        "entities": ("list", ("compound", mice)),
    }
    nbt.write_gzipped(path, root)


SYMBOLS = {"structure_void": " ", "air": ".", "spruce_planks": "#", "cobblestone": "c", "spruce_log": "L",
           "stripped_spruce_log": "T", "spruce_stairs": "s", "snow_block": "S", "glass_pane": "g",
           "spruce_door": "D", "wall_torch": "t", "bricks": "B", "campfire": "F", "spruce_leaves": "*",
           "red_wool": "r", "gold_block": "o", "shroomlight": "l", "glowstone": "G", "lime_wool": "p",
           "light_blue_wool": "p", "yellow_wool": "p", "cake": "k", "lantern": "i", "red_carpet": "_",
           "chest": "C"}


def preview():
    for y in (0, 1, 2, 5, 8):
        print(f"--- layer y={y} (north at top) ---")
        for z in range(SZ):
            print("".join(SYMBOLS[palette[grid[(x, y, z)]][0].split(":")[1]] for x in range(SX)))
    print("--- cross-section x=7 (north at left, up at top) ---")
    for y in reversed(range(SY)):
        print("".join(SYMBOLS[palette[grid[(7, y, z)]][0].split(":")[1]] for z in range(SZ)))


if __name__ == "__main__":
    build()
    if "--preview" in sys.argv:
        preview()
    write_nbt(sys.argv[1])
    solid = sum(1 for s in grid.values() if palette[s][0] not in ("minecraft:air", "minecraft:structure_void"))
    print("wrote", sys.argv[1], "with", solid, "blocks and", len(palette), "block states")
