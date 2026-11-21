#!/usr/bin/env python3
"""Generates the Christmas House structure template (15 x 13 x 15) as a Minecraft structure .nbt file.

Usage: python3 tools/generate_christmas_house.py src/main/resources/data/cheesedimension/structure/christmas_house.nbt [--preview]

A wooden house (spruce planks, spruce log corners, cobblestone foundation) with a gable roof of
spruce stairs and a snowy ridge. Inside: a brick fireplace with a campfire and a chimney, a
decorated Christmas tree with presents, a table with a cake, a rug, a loot chest and the two
Christmas mice who live there. The door is in the south wall and opens onto a small wooden porch.

Axes: x = east, z = south, y = up. The house walls are x 1-13, z 1-11; x 0/14 and z 0/12 hold the
roof overhang, z 12-13 the porch. The structure is rotated randomly when it is generated in the world.

Positions that the template does not set keep the existing terrain (they are simply left out of the
file, like vanilla does; a structure_void block in the file would be placed as a real, invisible block).
The script checks that the door and the space in front of and behind it are free.
"""
import sys

import nbt

SX, SY, SZ = 15, 13, 15
WALL_X0, WALL_X1 = 1, 13
WALL_Z0, WALL_Z1 = 1, 11
ROOF_Y = 5          # lowest roof row (also the top plate of the walls)
ROOF_Z1 = 12        # the roof covers z 0-12 (one block of overhang on each side)
RIDGE_Z = 6
DOOR_X = 7
PORCH_X0, PORCH_X1 = 5, 9
PORCH_Z0, PORCH_Z1 = 12, 13

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
    return ROOF_Y + min(z, ROOF_Z1 - z)


def leaves():
    return dict(distance=1, persistent=True, waterlogged=False)


def build():
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
        for z in range(ROOF_Z1 + 1):
            y = roof_height(z)
            if z < RIDGE_Z:
                put(x, y, z, "spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
            elif z > RIDGE_Z:
                put(x, y, z, "spruce_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
            else:
                put(x, y, z, "snow_block")

    porch()
    windows_and_door()
    fireplace()
    christmas_tree()
    furniture()
    check_door_access()


def porch():
    """Wooden porch in front of the door, with air above it, so the terrain never blocks the door."""
    for x in range(PORCH_X0, PORCH_X1 + 1):
        for z in range(PORCH_Z0, PORCH_Z1 + 1):
            put(x, 0, z, "spruce_planks")
            for y in range(1, ROOF_Y):
                if (x, y, z) not in grid:
                    put(x, y, z, "air")


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
    put(DOOR_X, 1, WALL_Z1, "spruce_door", half="lower", **door)
    put(DOOR_X, 2, WALL_Z1, "spruce_door", half="upper", **door)
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


def block_at(x, y, z):
    s = grid.get((x, y, z))
    return None if s is None else palette[s][0].split(":")[1]


def check_door_access():
    """Fails if anything blocks the door: the two cells inside and the two porch cells in front must be
    air (at both door heights) with a solid floor below, and the door itself must be complete."""
    assert block_at(DOOR_X, 1, WALL_Z1) == "spruce_door" and block_at(DOOR_X, 2, WALL_Z1) == "spruce_door"
    for z in (WALL_Z1 - 2, WALL_Z1 - 1, WALL_Z1 + 1, WALL_Z1 + 2):
        for y, passable in ((1, ("air", "red_carpet")), (2, ("air",))):   # a carpet can be walked over
            assert block_at(DOOR_X, y, z) in passable, f"door blocked at {(DOOR_X, y, z)}: {block_at(DOOR_X, y, z)}"
        assert block_at(DOOR_X, 0, z) in ("spruce_planks", "cobblestone"), f"no floor at {(DOOR_X, 0, z)}"


def write_nbt(path):
    blocks = []
    chest = state("minecraft:chest", facing="west", type="single", waterlogged=False)
    for (x, y, z), s in sorted(grid.items()):
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


SYMBOLS = {"air": ".", "spruce_planks": "#", "cobblestone": "c", "spruce_log": "L",
           "stripped_spruce_log": "T", "spruce_stairs": "s", "snow_block": "S", "glass_pane": "g",
           "spruce_door": "D", "wall_torch": "t", "bricks": "B", "campfire": "F", "spruce_leaves": "*",
           "red_wool": "r", "gold_block": "o", "shroomlight": "l", "glowstone": "G", "lime_wool": "p",
           "light_blue_wool": "p", "yellow_wool": "p", "cake": "k", "lantern": "i", "red_carpet": "_",
           "chest": "C"}


def symbol(x, y, z):
    """Preview character; a space means the position is not in the template (terrain stays)."""
    b = block_at(x, y, z)
    return " " if b is None else SYMBOLS[b]


def preview():
    for y in (0, 1, 2, 5, 8):
        print(f"--- layer y={y} (north at top) ---")
        for z in range(SZ):
            print("".join(symbol(x, y, z) for x in range(SX)))
    print(f"--- cross-section x={DOOR_X} through the door (north at left, up at top) ---")
    for y in reversed(range(SY)):
        print("".join(symbol(DOOR_X, y, z) for z in range(SZ)))


if __name__ == "__main__":
    build()
    if "--preview" in sys.argv:
        preview()
    write_nbt(sys.argv[1])
    solid = sum(1 for s in grid.values() if palette[s][0] != "minecraft:air")
    print("wrote", sys.argv[1], "with", solid, "blocks and", len(palette), "block states")
