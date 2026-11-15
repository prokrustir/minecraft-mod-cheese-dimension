#!/usr/bin/env python3
"""Generates the Cheesehenge structure template (25 x 10 x 25) as a Minecraft structure .nbt file.

Usage: python3 tools/generate_cheesehenge.py src/main/resources/data/cheesedimension/structure/cheesehenge.nbt [--preview]

A ring of 12 tall pillars with lintels (some broken, like a ruin), an inner horseshoe of five
trilithons open to the south, and a small altar in the middle, all on a round cheese platform.
Two Cheese Golems stand inside the ring. Axes: x = east, z = south, y = up; the structure is
rotated randomly when it is generated in the world.
"""
import math
import random
import sys

import nbt

SIZE_XZ = 25
SIZE_Y = 10
CENTER = 12
PLATFORM_RADIUS = 12.4   # round cheese platform at y = 0, flush with the ground

PALETTE_KEYS = ["air", "void", "cheese", "moldy"]
PALETTE = {
    "air": ("minecraft:air", {}),
    "void": ("minecraft:structure_void", {}),   # keeps the existing terrain outside the platform
    "cheese": ("cheesedimension:cheese_block", {}),
    "moldy": ("cheesedimension:moldy_cheese_block", {}),
}

# Cheese Golems placed with the template (x, y, z are inside the inner ring, standing on y = 1).
GOLEMS = [(9.5, 1.0, 14.5), (15.5, 1.0, 10.5)]

rnd = random.Random(777)
grid = {}


def put(x, y, z, key):
    if 0 <= x < SIZE_XZ and 0 <= z < SIZE_XZ and 0 <= y < SIZE_Y:
        grid[(x, y, z)] = key


def stone():
    """Mostly cheese with some moldy blocks mixed in."""
    return "moldy" if rnd.random() < 0.15 else "cheese"


def line(x0, z0, x1, z1):
    """Cells on the straight line between two cells, ends included."""
    steps = max(abs(x1 - x0), abs(z1 - z0), 1)
    return [(round(x0 + (x1 - x0) * i / steps), round(z0 + (z1 - z0) * i / steps)) for i in range(steps + 1)]


def build():
    # Platform: round disc at y = 0, everything else in the footprint is air, outside is "void".
    for x in range(SIZE_XZ):
        for z in range(SIZE_XZ):
            inside = math.hypot(x - CENTER, z - CENTER) <= PLATFORM_RADIUS
            put(x, 0, z, stone() if inside else "void")
            for y in range(1, SIZE_Y):
                put(x, y, z, "air" if inside else "void")

    # Outer ring: 12 pillars (2x2, normally 5 high) with lintels at y = 6; some are broken.
    pillars = []
    for k in range(12):
        angle = math.radians(k * 30 + 15)
        px = round(CENTER + 10 * math.cos(angle))
        pz = round(CENTER + 10 * math.sin(angle))
        height = rnd.choices([5, 4, 3], weights=[70, 15, 15])[0]
        pillars.append((px, pz, height))
        for dx in (0, 1):
            for dz in (0, 1):
                for y in range(1, height + 1):
                    put(px + dx, y, pz + dz, stone())
    for k in range(12):
        ax, az, ah = pillars[k]
        bx, bz, bh = pillars[(k + 1) % 12]
        if ah == 5 and bh == 5 and rnd.random() > 0.2:
            for x, z in line(ax, az, bx + 1, bz + 1):
                put(x, 6, z, stone())

    # Inner horseshoe: 5 trilithons (two 1x1 pillars 6 high with a lintel at y = 7), open to the south.
    for deg in (180, 225, 270, 315, 360):
        angle = math.radians(deg)
        cx = CENTER + 5 * math.cos(angle)
        cz = CENTER + 5 * math.sin(angle)
        tx, tz = -math.sin(angle), math.cos(angle)
        ax, az = round(cx - 1.5 * tx), round(cz - 1.5 * tz)
        bx, bz = round(cx + 1.5 * tx), round(cz + 1.5 * tz)
        for x, z in ((ax, az), (bx, bz)):
            for y in range(1, 7):
                put(x, y, z, stone())
        for x, z in line(ax, az, bx, bz):
            put(x, 7, z, stone())

    # Altar in the middle: 3x3 base with a taller block, topped by a moldy block.
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            put(CENTER + dx, 1, CENTER + dz, "cheese")
    put(CENTER, 2, CENTER, "cheese")
    put(CENTER, 3, CENTER, "moldy")


def write_nbt(path):
    palette = [nbt.palette_entry(*PALETTE[key]) for key in PALETTE_KEYS]
    blocks = []
    for x in range(SIZE_XZ):
        for y in range(SIZE_Y):
            for z in range(SIZE_XZ):
                key = grid.get((x, y, z), "air")
                blocks.append({"pos": ("list", ("int", [x, y, z])),
                               "state": ("int", PALETTE_KEYS.index(key))})
    golems = [{
        "pos": ("list", ("double", [gx, gy, gz])),
        "blockPos": ("list", ("int", [int(gx), int(gy), int(gz)])),
        "nbt": ("compound", {
            "id": ("string", "cheesedimension:cheese_golem"),
            "PersistenceRequired": ("byte", 1),
        }),
    } for gx, gy, gz in GOLEMS]
    root = {
        "DataVersion": ("int", nbt.DATA_VERSION),
        "size": ("list", ("int", [SIZE_XZ, SIZE_Y, SIZE_XZ])),
        "palette": ("list", ("compound", palette)),
        "blocks": ("list", ("compound", blocks)),
        "entities": ("list", ("compound", golems)),
    }
    nbt.write_gzipped(path, root)


def preview():
    symbols = {"air": ".", "void": " ", "cheese": "#", "moldy": "m"}
    for y in (0, 1, 6, 7):
        print(f"--- layer y={y} (north at top) ---")
        for z in range(SIZE_XZ):
            print("".join(symbols[grid.get((x, y, z), "air")] for x in range(SIZE_XZ)))


if __name__ == "__main__":
    build()
    if "--preview" in sys.argv:
        preview()
    write_nbt(sys.argv[1])
    print("wrote", sys.argv[1], "with", sum(1 for k in grid.values() if k not in ("air", "void")), "solid blocks")
