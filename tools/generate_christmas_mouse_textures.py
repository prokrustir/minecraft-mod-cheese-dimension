#!/usr/bin/env python3
"""Paints the 64x64 textures of the two Christmas mice (elf mouse and Christmas mouse).

Usage: python3 tools/generate_christmas_mouse_textures.py src/main/resources/assets/cheesedimension/textures/entity [preview.png]

The uv layout must match client/ChristmasMouseModel.java: the standard biped parts in the top half
(head 0,0; body 16,16; arms 40,16; legs 0,16) and the extra parts below them. With a second
argument the script also writes an 8x enlarged preview of both textures side by side.
"""
import random
import struct
import sys
import zlib

W = H = 64


def write_png(path, width, height, pixels):
    raw = b"".join(b"\x00" + b"".join(struct.pack("BBBB", *pixels[y * width + x]) for x in range(width))
                   for y in range(height))

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n")
        f.write(chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)))
        f.write(chunk(b"IDAT", zlib.compress(raw, 9)))
        f.write(chunk(b"IEND", b""))


def faces(u, v, w, h, d):
    """Texture rectangles (x0, y0, x1, y1) of a cuboid with size w x h x d at uv (u, v)."""
    return {
        "top": (u + d, v, u + d + w, v + d),
        "bottom": (u + d + w, v, u + d + 2 * w, v + d),
        "right": (u, v + d, u + d, v + d + h),
        "front": (u + d, v + d, u + d + w, v + d + h),
        "left": (u + d + w, v + d, u + 2 * d + w, v + d + h),
        "back": (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h),
    }


SIDES = ("right", "front", "left", "back")

# uv positions and sizes, as in ChristmasMouseModel
HEAD = faces(0, 0, 8, 8, 8)
BODY = faces(16, 16, 8, 12, 4)
ARM = faces(40, 16, 4, 12, 4)
LEG = faces(0, 16, 4, 12, 4)
EAR = faces(0, 32, 5, 5, 1)
SNOUT = faces(12, 32, 3, 2, 2)
TAIL = faces(22, 32, 1, 1, 8)
HAT_BASE = faces(0, 42, 8, 3, 8)
HAT_MIDDLE = faces(32, 42, 5, 3, 5)
HAT_TOP = faces(52, 42, 3, 3, 3)
HAT_TIP = faces(56, 48, 2, 5, 2)
SKIRT = faces(0, 54, 9, 5, 5)
SCARF = faces(28, 54, 9, 2, 5)

BLACK = (24, 20, 20)


class Texture:
    def __init__(self, seed):
        self.img = [(0, 0, 0, 0)] * (W * H)
        self.rnd = random.Random(seed)

    def px(self, x, y, c):
        self.img[y * W + x] = (c[0], c[1], c[2], 255)

    def color(self, palette):
        return self.rnd.choice(palette) if isinstance(palette, list) else palette

    def fill(self, box, palette):
        x0, y0, x1, y1 = box
        for y in range(y0, y1):
            for x in range(x0, x1):
                self.px(x, y, self.color(palette))

    def paint(self, cuboid, palette, sides=None):
        for name, box in cuboid.items():
            if sides is None or name in sides:
                self.fill(box, palette)

    def rows(self, cuboid, painter):
        """Calls painter(row) for each row of the side faces, painter returns a palette."""
        for name in SIDES:
            x0, y0, x1, y1 = cuboid[name]
            for y in range(y0, y1):
                self.fill((x0, y, x1, y + 1), painter(y - y0))

    def face(self, fur, nose_color):
        x0, y0, x1, y1 = HEAD["front"]
        for ex in (x0 + 2, x0 + 5):
            self.px(ex, y0 + 4, BLACK)          # small black bead eyes
        sx0, sy0, sx1, sy1 = SNOUT["front"]
        self.px(sx0 + 1, sy0, nose_color)       # nose at the tip of the snout


def elf_mouse():
    """Green elf mouse: brown felt fur, moss-green fuzzy hat and jumper, knitted scarf, beige skirt,
    green and white striped sleeves and stockings, beige shoes."""
    t = Texture(25)
    fur = [(176, 146, 118), (168, 138, 110), (184, 154, 126)]
    ear_inside = [(120, 66, 44), (110, 58, 38), (126, 72, 48)]
    moss = [(78, 96, 40), (70, 88, 34), (86, 104, 46), (64, 80, 30)]
    mitten = [(96, 128, 58), (90, 120, 54)]
    cream = [(226, 220, 200), (232, 226, 208)]
    stripe_green = [(72, 92, 44), (66, 86, 40)]
    knit = [(150, 168, 108), (132, 150, 92), (168, 184, 124)]
    felt = [(204, 172, 132), (196, 164, 124), (210, 180, 140)]
    shoe = [(196, 168, 132), (188, 160, 124)]

    t.paint(HEAD, fur)
    t.face(fur, BLACK)
    t.paint(SNOUT, fur)
    t.paint(EAR, fur)
    x0, y0, x1, y1 = EAR["front"]
    t.fill((x0 + 1, y0 + 1, x1 - 1, y1 - 1), ear_inside)
    t.paint(TAIL, [(180, 140, 120), (172, 132, 112)])

    t.paint(HAT_BASE, moss)
    t.rows(HAT_BASE, lambda r: [(214, 200, 176), (204, 190, 166)] if r == 2 else moss)  # fluffy rim
    t.paint(HAT_MIDDLE, moss)
    t.paint(HAT_TOP, moss)
    t.paint(HAT_TIP, moss)

    t.paint(BODY, moss)
    x0, y0, x1, y1 = BODY["front"]
    for by in (y0 + 3, y0 + 6):
        t.px(x0 + 5, by, (222, 196, 150))       # wooden buttons
    t.paint(SCARF, knit)
    for name in SIDES:                          # chunky knit pattern
        x0, y0, x1, y1 = SCARF[name]
        for y in range(y0, y1):
            for x in range(x0, x1):
                if (x + y) % 3 == 0:
                    t.px(x, y, (110, 128, 74))

    t.paint(ARM, cream, ("top",))
    t.rows(ARM, lambda r: mitten if r >= 9 else (stripe_green if r % 2 == 0 else cream))
    t.fill(ARM["bottom"], mitten)

    t.paint(LEG, moss, ("top",))
    t.rows(LEG, lambda r: shoe if r >= 10 else moss if r >= 7 else (stripe_green if r % 2 == 0 else cream))
    t.fill(LEG["bottom"], shoe)

    t.paint(SKIRT, felt)
    t.rows(SKIRT, lambda r: [(226, 204, 170)] if r == 3 else felt)  # stitched hem
    return t.img


def christmas_mouse():
    """Christmas mouse: cream linen fur, long burgundy knitted hat and jumper with a roll neck,
    brown plaid skirt, cream legs."""
    t = Texture(31)
    linen = [(226, 214, 192), (218, 206, 184), (232, 222, 202)]
    ear_inside = [(222, 186, 172), (214, 178, 164)]
    burgundy = [(118, 24, 40), (110, 20, 36), (126, 30, 46)]
    burgundy_dark = (92, 16, 30)

    t.paint(HEAD, linen)
    t.face(linen, BLACK)
    t.paint(SNOUT, linen)
    t.paint(EAR, linen)
    x0, y0, x1, y1 = EAR["front"]
    t.fill((x0 + 1, y0 + 1, x1 - 1, y1 - 1), ear_inside)
    t.paint(TAIL, linen)

    for part in (HAT_BASE, HAT_MIDDLE, HAT_TOP, HAT_TIP):
        t.paint(part, burgundy)
        for name in SIDES:                      # vertical knit ribs
            x0, y0, x1, y1 = part[name]
            for x in range(x0, x1, 2):
                for y in range(y0, y1):
                    t.px(x, y, burgundy_dark)

    t.paint(BODY, burgundy)
    x0, y0, x1, y1 = BODY["front"]
    for y in range(y0 + 2, y1 - 1):             # cable knit down the front
        t.px(x0 + 2 + (y % 2), y, burgundy_dark)
        t.px(x1 - 3 - (y % 2), y, burgundy_dark)
    t.paint(SCARF, [(132, 34, 50), (124, 28, 44)])   # roll neck collar
    t.rows(SCARF, lambda r: burgundy_dark if r == 1 else [(132, 34, 50), (124, 28, 44)])

    t.paint(ARM, burgundy, ("top",))
    t.rows(ARM, lambda r: linen if r >= 10 else burgundy)
    t.fill(ARM["bottom"], linen)

    t.paint(LEG, linen)

    # Plaid skirt: brown base with dark, burgundy and grey lines.
    t.paint(SKIRT, [(112, 88, 74), (106, 82, 68)])
    for name in SKIRT:
        x0, y0, x1, y1 = SKIRT[name]
        for y in range(y0, y1):
            for x in range(x0, x1):
                if (x - x0) % 4 == 0 or (y - y0) % 4 == 0:
                    t.px(x, y, (64, 48, 42))
                elif (x - x0) % 4 == 2:
                    t.px(x, y, (126, 44, 54))
                elif (y - y0) % 4 == 2:
                    t.px(x, y, (156, 146, 134))
    return t.img


def main():
    out = sys.argv[1]
    textures = [("elf_mouse", elf_mouse()), ("christmas_mouse", christmas_mouse())]
    for name, img in textures:
        write_png(f"{out}/{name}.png", W, H, img)
    if len(sys.argv) > 2:
        scale = 8
        gap = 8
        width = 2 * W * scale + gap
        big = []
        for y in range(H * scale):
            for i, (_, img) in enumerate(textures):
                for x in range(W * scale):
                    p = img[(y // scale) * W + x // scale]
                    big.append(p if p[3] else (40, 60, 40, 255))
                if i == 0:
                    big.extend([(0, 0, 0, 255)] * gap)
        write_png(sys.argv[2], width, H * scale, big)
    print("wrote", ", ".join(name for name, _ in textures))


if __name__ == "__main__":
    main()
