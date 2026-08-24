# -*- coding: utf-8 -*-
"""Control de las capturas antes de subirlas.

Existe porque **una captura mala no da error**: sale negra, o a medio pintar, o en
el idioma equivocado, y se sube igual. Devuelve 1 si algo falla, asi que sirve tal
cual dentro de todos.sh.

    python3 revisar.py ../capturas
"""
import argparse
import os
import struct
import sys
import zlib

import tanda

# Lo que Play espera de cada formato. La de tablet tiene que ser 9:16 exacto.
EXPECTED = {
    "telefono": (1080, 2400),
    "tablet7": (1080, 1920),
    "tablet10": (1440, 2560),
}
LANGUAGES = ("es", "en")


def read_png(path):
    """Dimensiones y pixeles de un PNG, sin dependencias externas.

    Sin Pillow a proposito: este script tiene que poder correr en cualquier maquina
    con Python y nada mas, o dejara de correrse.
    """
    with open(path, "rb") as handle:
        data = handle.read()
    if not data.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ValueError("no es un PNG")
    width = height = None
    idat = b""
    pos = 8
    bit_depth = color_type = None
    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        kind = data[pos + 4:pos + 8]
        payload = data[pos + 8:pos + 8 + length]
        if kind == b"IHDR":
            width, height, bit_depth, color_type = struct.unpack(">IIBB", payload[:10])
        elif kind == b"IDAT":
            idat += payload
        elif kind == b"IEND":
            break
        pos += length + 12
    return width, height, bit_depth, color_type, idat


def pixel_rows(path):
    """Devuelve las filas ya sin filtro PNG, en bytes por pixel."""
    width, height, bit_depth, color_type, idat = read_png(path)
    if bit_depth != 8:
        raise ValueError("profundidad no soportada: %s" % bit_depth)
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[color_type]
    raw = zlib.decompress(idat)
    stride = width * channels
    rows = []
    previous = bytearray(stride)
    pos = 0
    for _ in range(height):
        filter_type = raw[pos]
        line = bytearray(raw[pos + 1:pos + 1 + stride])
        pos += 1 + stride
        for i in range(stride):
            a = line[i - channels] if i >= channels else 0
            b = previous[i]
            c = previous[i - channels] if i >= channels else 0
            if filter_type == 1:
                line[i] = (line[i] + a) & 0xFF
            elif filter_type == 2:
                line[i] = (line[i] + b) & 0xFF
            elif filter_type == 3:
                line[i] = (line[i] + (a + b) // 2) & 0xFF
            elif filter_type == 4:
                delta = a + b - c
                pa, pb, pc = abs(delta - a), abs(delta - b), abs(delta - c)
                pred = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pred) & 0xFF
        rows.append(bytes(line))
        previous = line
    return width, height, channels, rows


def variance(path, sample_every=17):
    """Cuanto varia la imagen. Una captura negra o a medio pintar apenas varia."""
    width, height, channels, rows = pixel_rows(path)
    values = []
    for y in range(0, height, sample_every):
        row = rows[y]
        for x in range(0, width, sample_every):
            i = x * channels
            values.append((row[i] + row[i + 1] + row[i + 2]) / 3)
    mean = sum(values) / len(values)
    return sum((v - mean) ** 2 for v in values) / len(values)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("raiz", help="carpeta con telefono/, tablet7/ y tablet10/")
    parser.add_argument("--formato", action="append", choices=sorted(EXPECTED),
                        help="revisar solo estos formatos")
    args = parser.parse_args()

    formats = args.formato or sorted(EXPECTED)
    problems = []
    checked = 0

    for fmt in formats:
        expected = EXPECTED[fmt]
        for scene in tanda.SCENES:
            digests = {}
            for language in LANGUAGES:
                path = os.path.join(args.raiz, fmt, language, scene + ".png")
                if not os.path.exists(path):
                    problems.append("falta %s" % path)
                    continue
                checked += 1
                width, height, _, _, _ = read_png(path)
                if (width, height) != expected:
                    problems.append("%s mide %dx%d y deberia ser %dx%d"
                                    % (path, width, height, expected[0], expected[1]))
                var = variance(path)
                if var < 40:
                    problems.append("%s parece vacia o a medio pintar (varianza %.1f)"
                                    % (path, var))
                with open(path, "rb") as handle:
                    digests[language] = zlib.crc32(handle.read())
            # Un idioma colado: las dos capturas de la misma escena serian identicas.
            if len(digests) == 2 and len(set(digests.values())) == 1:
                problems.append("%s/%s: las capturas es y en son identicas, "
                                "hay un idioma colado" % (fmt, scene))

    print("revisadas %d capturas" % checked)
    expected_total = len(formats) * len(LANGUAGES) * len(tanda.SCENES)
    if checked != expected_total:
        problems.append("esperaba %d capturas y hay %d" % (expected_total, checked))
    for problem in problems:
        print("  FALLO: %s" % problem)
    if problems:
        return 1
    print("  todo correcto")
    return 0


if __name__ == "__main__":
    sys.exit(main())
