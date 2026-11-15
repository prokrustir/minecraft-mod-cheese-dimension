"""Minimal NBT writer shared by the structure generators in this folder.

Values are (tag, value) pairs, e.g. ("int", 3), ("list", ("int", [1, 2, 3])) or
("compound", {"key": ("string", "text")}). Supported tags: byte, int, double, string, list, compound.
"""
import gzip
import struct

TAG_IDS = {"byte": 1, "int": 3, "double": 6, "string": 8, "list": 9, "compound": 10}

DATA_VERSION = 3955  # Minecraft 1.21.1


def _string(value):
    raw = value.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def _payload(tag, value):
    if tag == "byte":
        return struct.pack(">b", value)
    if tag == "int":
        return struct.pack(">i", value)
    if tag == "double":
        return struct.pack(">d", value)
    if tag == "string":
        return _string(value)
    if tag == "list":
        element_tag, items = value
        return (bytes([TAG_IDS[element_tag]]) + struct.pack(">i", len(items))
                + b"".join(_payload(element_tag, item) for item in items))
    if tag == "compound":
        out = b""
        for key, (child_tag, child_value) in value.items():
            out += bytes([TAG_IDS[child_tag]]) + _string(key) + _payload(child_tag, child_value)
        return out + b"\x00"
    raise ValueError(tag)


def write_gzipped(path, root):
    """Writes the root compound (a dict of name -> (tag, value)) as a gzipped .nbt file."""
    data = bytes([TAG_IDS["compound"]]) + _string("") + _payload("compound", root)
    with open(path, "wb") as raw:
        # mtime=0 keeps the output byte-for-byte reproducible
        with gzip.GzipFile(fileobj=raw, mode="wb", mtime=0) as gz:
            gz.write(data)


def palette_entry(name, props):
    entry = {"Name": ("string", name)}
    if props:
        entry["Properties"] = ("compound", {k: ("string", v) for k, v in props.items()})
    return entry
