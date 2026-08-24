# -*- coding: utf-8 -*-
"""Genera el icono de Sleep Noise —«Luna de grano»— en todas las salidas que
hacen falta, desde una sola definición geométrica:

  docs/design/icono/Main.dc.html      artboard para el lienzo de diseño
  docs/design/iconos-sleep-noise.html página de presentación
  docs/design/icono/vector/*.xml      VectorDrawable listos para res/drawable
  docs/design/icono/play-icon-512.svg origen del PNG de la ficha de Play

El grano se calcula aquí con un PRNG determinista en vez de dibujarse con un
filtro SVG: un VectorDrawable de Android no soporta filtros, así que lo que se
ve en la maqueta es exactamente lo que se puede exportar. Y determinista porque
el mismo grano tiene que salir en cada regeneración, o el icono cambiaría solo
entre commits.
"""
import math, pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "docs/design/icono"
VEC = OUT / "vector"
OUT.mkdir(parents=True, exist_ok=True)
VEC.mkdir(parents=True, exist_ok=True)

# Paleta de «Noche profunda». Hex, no oklch: un VectorDrawable tampoco lo entiende.
BG_IN, BG_OUT = "#251D16", "#150F0C"
ACCENT = "#E8A860"

CANVAS = 108.0   # lienzo del icono adaptativo, en dp
SAFE_R = 33.0    # radio de la zona segura recomendada (66 dp de diámetro)

# Creciente: disco grande menos disco recortante. Una sola definición para el
# grano, la silueta monocroma y el icono de notificación.
MOON = dict(R=30.0, cx=52.0, cy=54.0, r2=25.0, c2x=64.0, c2y=47.0)


class Rng:
    def __init__(self, seed):
        self.s = seed
    def next(self):
        self.s = (self.s * 1103515245 + 12345) & 0x7FFFFFFF
        return self.s / 0x7FFFFFFF


def circle_intersections(c1, r1, c2, r2):
    (x1, y1), (x2, y2) = c1, c2
    dx, dy = x2 - x1, y2 - y1
    d = math.hypot(dx, dy)
    assert abs(r1 - r2) < d < r1 + r2, "los circulos no se cortan"
    a = (d * d + r1 * r1 - r2 * r2) / (2 * d)
    h = math.sqrt(r1 * r1 - a * a)
    px, py = x1 + a * dx / d, y1 + a * dy / d
    ux, uy = -dy / d, dx / d
    return (px + h * ux, py + h * uy), (px - h * ux, py - h * uy)


def crescent_path(scale=1.0, ox=0.0, oy=0.0):
    """Escrito a mano, el segundo radio salía menor que media cuerda y SVG lo
    reescalaba hasta convertir la luna en una lente. Aquí se deriva."""
    m = MOON
    c1, c2 = (m["cx"], m["cy"]), (m["c2x"], m["c2y"])
    i1, i2 = circle_intersections(c1, m["R"], c2, m["r2"])
    if i1[1] < i2[1]:
        i1, i2 = i2, i1
    def t(p):
        return (p[0] * scale + ox, p[1] * scale + oy)
    a, b = t(i2), t(i1)
    R, r2 = m["R"] * scale, m["r2"] * scale
    return ("M%.2f %.2f A%.2f %.2f 0 1 0 %.2f %.2f A%.2f %.2f 0 1 1 %.2f %.2f Z"
            % (a[0], a[1], R, R, b[0], b[1], r2, r2, a[0], a[1]))


def inside_crescent(x, y, grow=0.0):
    m = MOON
    d1 = math.hypot(x - m["cx"], y - m["cy"])
    d2 = math.hypot(x - m["c2x"], y - m["c2y"])
    return d1 <= m["R"] + grow and d2 >= m["r2"]


def grain_dots(seed=7, target=230):
    """El grano del primer plano: puntos finos dentro del creciente."""
    rng = Rng(seed)
    m = MOON
    dots = []
    for _ in range(6000):
        x = m["cx"] - m["R"] + rng.next() * 2 * m["R"]
        y = m["cy"] - m["R"] + rng.next() * 2 * m["R"]
        if not inside_crescent(x, y):
            continue
        edge = min(m["R"] - math.hypot(x - m["cx"], y - m["cy"]),
                   math.hypot(x - m["c2x"], y - m["c2y"]) - m["r2"])
        if edge < 0.6 and rng.next() < 0.6:
            continue
        dots.append((x, y, 0.5 + rng.next() * 1.15))
        if len(dots) >= target:
            break
    return dots


def rim_dots(seed=41, target=74):
    """Grano GRUESO pegado al borde exterior, para la capa monocroma: conserva la
    textura a tamaño grande y se compacta en una luna a tamaño pequeño, que es
    justo lo que el grano fino no consigue en una sola tinta."""
    rng = Rng(seed)
    m = MOON
    dots = []
    for _ in range(9000):
        ang = rng.next() * 2 * math.pi
        rad = m["R"] + (rng.next() ** 0.6) * 2.6
        x, y = m["cx"] + rad * math.cos(ang), m["cy"] + rad * math.sin(ang)
        if math.hypot(x - m["c2x"], y - m["c2y"]) < m["r2"] + 1.2:
            continue
        if math.hypot(x - 54, y - 54) > SAFE_R - 0.9:
            continue
        dots.append((x, y, 1.2 + rng.next() * 1.0))
        if len(dots) >= target:
            break
    return dots


GRAIN = grain_dots()
RIM = rim_dots()

# ------------------------------------------------------------------ SVG parts
def svg_bg(uid):
    return ('<defs><radialGradient id="bg%s" cx="50%%" cy="42%%" r="72%%">'
            '<stop offset="0" stop-color="%s"/><stop offset="1" stop-color="%s"/>'
            '</radialGradient></defs>'
            '<rect width="108" height="108" fill="url(#bg%s)"/>' % (uid, BG_IN, BG_OUT, uid))


def svg_grain(fill=ACCENT):
    return '<g fill="%s">%s</g>' % (
        fill, "".join('<circle cx="%.2f" cy="%.2f" r="%.2f"/>' % d for d in GRAIN))


def svg_mono():
    """Creciente sólido con el borde exterior granulado."""
    return ('<g fill="#FFFFFF"><path d="%s"/>%s</g>'
            % (crescent_path(),
               "".join('<circle cx="%.2f" cy="%.2f" r="%.2f"/>' % d for d in RIM)))


def svg(size, layer="color", uid=""):
    if layer == "color":
        body = svg_bg(uid) + svg_grain()
    elif layer == "mono":
        body = '<rect width="108" height="108" fill="#6F6A66"/>' + svg_mono()
    elif layer == "fg":
        body = svg_grain()
    else:
        body = svg_bg(uid)
    return ('<svg width="%d" height="%d" viewBox="0 0 108 108" '
            'xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Sleep Noise">%s</svg>'
            % (size, size, body))


def svg_full(size):
    """El lienzo entero con lo que la máscara recorta marcado. Es la vista que
    evita el error clásico: contenido demasiado grande."""
    body = svg_bg("full") + svg_grain()
    body += ('<rect x="18" y="18" width="72" height="72" rx="20" fill="none" '
             'stroke="#FFFFFF" stroke-opacity="0.42" stroke-width="0.7" stroke-dasharray="3 3"/>')
    body += ('<circle cx="54" cy="54" r="%.1f" fill="none" stroke="#E8A860" '
             'stroke-opacity="0.8" stroke-width="0.7" stroke-dasharray="2.4 2.4"/>' % SAFE_R)
    return ('<svg width="%d" height="%d" viewBox="0 0 108 108" '
            'xmlns="http://www.w3.org/2000/svg" role="img" '
            'aria-label="Lienzo completo de 108 dp">%s</svg>' % (size, size, body))


def svg_notif(size):
    """Icono de notificación: 24 dp, silueta en una tinta, sin fondo ni grano —
    a 24 dp el grano no existe. El mordisco se agranda para que se lea."""
    return ('<svg width="%d" height="%d" viewBox="0 0 24 24" '
            'xmlns="http://www.w3.org/2000/svg" role="img" '
            'aria-label="Icono de notificacion"><path d="%s" fill="#FFFFFF"/></svg>'
            % (size, size, notif_path()))


def notif_path():
    """Mismo creciente, reescalado a 24 dp con 2 dp de margen."""
    m = MOON
    scale = 20.0 / (2 * m["R"])
    ox = 12.0 - m["cx"] * scale
    oy = 12.0 - m["cy"] * scale
    return crescent_path(scale=scale, ox=ox, oy=oy)


def masked(size, shape, layer="color"):
    """Reproduce lo que hace Android: compone en 108 dp y enseña los 72 centrales."""
    radius = {"circle": "50%", "squircle": "28%", "square": "16%"}[shape]
    inner = int(round(size * 1.5))
    off = -(inner - size) / 2
    return ('<div style="width:%dpx;height:%dpx;border-radius:%s;overflow:hidden;'
            'position:relative;flex-shrink:0;">'
            '<div style="position:absolute;left:%.1fpx;top:%.1fpx;">%s</div></div>'
            % (size, size, radius, off, off, svg(inner, layer=layer, uid=shape + str(size) + layer)))


def cell(label, inner):
    return ('<div style="display:flex;flex-direction:column;align-items:center;gap:9px;">'
            '%s<span class="cap">%s</span></div>' % (inner, label))


# ------------------------------------------------------- VectorDrawable (XML)
def dots_pathdata(dots):
    """Un circulo como subpath: dos arcos. Todos los puntos van en UN path, que
    es mas barato de inflar que 230 elementos."""
    out = []
    for x, y, r in dots:
        out.append("M%.2f,%.2f a%.2f,%.2f 0 1,0 %.2f,0 a%.2f,%.2f 0 1,0 %.2f,0 Z"
                   % (x - r, y, r, r, 2 * r, r, r, -2 * r))
    return " ".join(out)


VEC_HEAD = ('<?xml version="1.0" encoding="utf-8"?>\n'
            '<!-- Generado por scripts/generar-iconos.py. No editar a mano. -->\n')

(VEC / "ic_launcher_background.xml").write_text(VEC_HEAD +
"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:aapt="http://schemas.android.com/aapt"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <path android:pathData="M0,0h108v108h-108z">
        <aapt:attr name="android:fillColor">
            <gradient
                android:type="radial"
                android:centerX="54"
                android:centerY="45.36"
                android:gradientRadius="77.76">
                <item android:offset="0" android:color="%s"/>
                <item android:offset="1" android:color="%s"/>
            </gradient>
        </aapt:attr>
    </path>
</vector>
""" % (BG_IN, BG_OUT))

(VEC / "ic_launcher_foreground.xml").write_text(VEC_HEAD +
"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- %d puntos de grano dentro del creciente, en un solo path. -->
    <path
        android:fillColor="%s"
        android:pathData="%s"/>
</vector>
""" % (len(GRAIN), ACCENT, dots_pathdata(GRAIN)))

(VEC / "ic_launcher_monochrome.xml").write_text(VEC_HEAD +
"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- El grano fino desaparece en una sola tinta: aqui el creciente es solido
         y la textura vive en %d puntos gruesos pegados al borde exterior. -->
    <path android:fillColor="#FFFFFF" android:pathData="%s"/>
    <path android:fillColor="#FFFFFF" android:pathData="%s"/>
</vector>
""" % (len(RIM), crescent_path(), dots_pathdata(RIM)))

(VEC / "ic_stat_sleep_noise.xml").write_text(VEC_HEAD +
"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24"
    android:tint="?attr/colorControlNormal">
    <!-- Icono de la barra de estado: silueta, sin fondo y sin grano. A 24 dp el
         grano no existe, y el sistema tinta esta capa. -->
    <path android:fillColor="#FFFFFF" android:pathData="%s"/>
</vector>
""" % notif_path())

(VEC / "ic_launcher.xml").write_text(VEC_HEAD +
"""<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
    <monochrome android:drawable="@drawable/ic_launcher_monochrome"/>
</adaptive-icon>
""")

(OUT / "play-icon-512.svg").write_text(
    '<!-- Origen del PNG 512x512 de la ficha de Play. Sin mascara: la pone Play. -->\n'
    + svg(512, layer="color", uid="play") + "\n")

# ------------------------------------------------------------------- artboard
art = """<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Sora:wght@200;400;600&family=IBM+Plex+Mono:wght@400;500&display=swap">
  <style>
    body { margin: 0; }
    a { color: #E8A860; } a:hover { color: #F2C391; }
    .cap { font-family: "IBM Plex Mono", monospace; font-size: 9px; letter-spacing: 0.13em;
           text-transform: uppercase; color: #8A8078; }
  </style>
</helmet>
<div style="width: 470px; height: 640px; box-sizing: border-box; background: #12100E; font-family: Sora, system-ui, sans-serif; display: flex; flex-direction: column; align-items: center; padding: 40px 32px; gap: 28px;">
  <div style="display: flex; flex-direction: column; align-items: center; gap: 7px;">
    <span style="font-family: 'IBM Plex Mono', monospace; font-size: 10px; letter-spacing: 0.2em; color: #E8A860;">ICONO APROBADO</span>
    <span style="font-size: 25px; font-weight: 200; letter-spacing: -0.015em; color: #F0E9E2;">Luna de grano</span>
  </div>
  @@HERO@@
  <div style="display: flex; gap: 20px; align-items: flex-end;">@@MASKS@@</div>
  <div style="display: flex; gap: 20px; align-items: flex-end;">@@TESTS@@</div>
  <div style="font-family: 'IBM Plex Mono', monospace; font-size: 9.5px; letter-spacing: 0.06em; color: #8A8078; text-align: center; max-width: 340px; line-height: 1.6;">@@FOOT@@</div>
</div>
</x-dc>
<script data-dc-script data-props='{"$preview":{"width":470,"height":640}}'>
class Component extends DCLogic {}
</script>
</body>
</html>
"""
masks = "".join([cell("Círculo", masked(66, "circle")),
                 cell("Squircle", masked(66, "squircle")),
                 cell("Cuadrado", masked(66, "square"))])
tests = "".join([cell("48 dp real", masked(48, "squircle")),
                 cell("Monocromo", masked(66, "squircle", layer="mono")),
                 cell("Notificación 24 dp",
                      '<div style="width:66px;height:66px;display:flex;align-items:center;'
                      'justify-content:center;background:#221C17;border-radius:10px;">%s</div>'
                      % svg_notif(24)),
                 cell("Lienzo 108", svg_full(88))])
for token, value in [("@@HERO@@", masked(176, "squircle")), ("@@MASKS@@", masks),
                     ("@@TESTS@@", tests),
                     ("@@FOOT@@", "%d puntos de grano · creciente derivado, no dibujado a mano · "
                                  "monocromo con borde granulado" % len(GRAIN))]:
    art = art.replace(token, value)
(OUT / "Main.dc.html").write_text(art)

(OUT / "canvas.json").write_text(
    '{\n  "artboards": [\n'
    '    { "file": "Main.dc.html", "title": "Icono · Luna de grano", '
    '"x": 0, "y": 0, "w": 470, "h": 640 }\n  ],\n'
    '  "annotations": [\n'
    '    { "id": "note-icono", "x": 550, "y": 0, "w": 400, "text": '
    '"ICONO APROBADO · LUNA DE GRANO\\n\\nElegido el 2026-08-24 entre cuatro propuestas. '
    'Las otras tres (el aro, la onda que se apaga, el campo de ruido) quedan descartadas.\\n\\n'
    'El pendiente que traía esta propuesta era el monocromo: el grano fino desaparece en una sola '
    'tinta. Resuelto con un creciente solido y el borde exterior granulado, que conserva la textura '
    'en grande y se compacta en una luna en pequeno.\\n\\n'
    'Assets en docs/design/icono/vector/. Decision en docs/decisions/004." }\n  ],\n'
    '  "launch": { "view": "focused", "file": "Main.dc.html" }\n}\n')

for stale in ("IconoA.dc.html", "IconoB.dc.html", "IconoC.dc.html", "IconoD.dc.html"):
    (OUT / stale).unlink(missing_ok=True)


# ---------------------------------------------------------------------- página
STRIP = "".join([
    cell("Círculo", masked(68, "circle")),
    cell("Squircle", masked(68, "squircle")),
    cell("Cuadrado", masked(68, "square")),
    cell("48 dp real", masked(48, "squircle")),
    cell("Lienzo 108 dp", svg_full(96)),
])

FAMILY = "".join([
    cell("Fondo", masked(96, "squircle", layer="bg")),
    cell("Primer plano", '<div style="width:96px;height:96px;border-radius:28%%;'
                         'background:#F3EFEA;overflow:hidden;position:relative;">'
                         '<div style="position:absolute;left:-24px;top:-24px;">%s</div></div>'
                         % svg(144, layer="fg", uid="fam")),
    cell("Monocromo", masked(96, "squircle", layer="mono")),
    cell("Notificación 24 dp",
         '<div style="width:96px;height:96px;display:flex;align-items:center;'
         'justify-content:center;background:#221C17;border-radius:16px;">%s</div>' % svg_notif(24)),
])

page = """<title>El icono de Sleep Noise</title>
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Sora:wght@200;400;600&family=Newsreader:opsz,wght@6..72,300;6..72,400;6..72,500&family=IBM+Plex+Mono:wght@400;500&display=swap">
<style>
  :root {
    --bg: oklch(0.985 0.006 70); --panel: oklch(1 0 0);
    --ink: oklch(0.24 0.014 62); --muted: oklch(0.5 0.014 62);
    --faint: oklch(0.63 0.012 62); --line: oklch(0.9 0.008 62);
    --accent: oklch(0.55 0.11 62); --accent-soft: oklch(0.955 0.03 70);
    --code-bg: oklch(0.955 0.008 62);
  }
  @media (prefers-color-scheme: dark) {
    :root:not([data-theme="light"]) {
      --bg: oklch(0.155 0.014 62); --panel: oklch(0.192 0.014 62);
      --ink: oklch(0.93 0.008 62); --muted: oklch(0.72 0.012 62);
      --faint: oklch(0.58 0.014 62); --line: oklch(0.28 0.012 62);
      --accent: oklch(0.78 0.1 68); --accent-soft: oklch(0.245 0.03 62);
      --code-bg: oklch(0.225 0.012 62);
    }
  }
  :root[data-theme="dark"] {
    --bg: oklch(0.155 0.014 62); --panel: oklch(0.192 0.014 62);
    --ink: oklch(0.93 0.008 62); --muted: oklch(0.72 0.012 62);
    --faint: oklch(0.58 0.014 62); --line: oklch(0.28 0.012 62);
    --accent: oklch(0.78 0.1 68); --accent-soft: oklch(0.245 0.03 62);
    --code-bg: oklch(0.225 0.012 62);
  }
  * { box-sizing: border-box; }
  body {
    margin: 0; background: var(--bg); color: var(--ink);
    font-family: Newsreader, Georgia, serif; font-size: 17px; line-height: 1.6;
    -webkit-font-smoothing: antialiased;
  }
  .shell { max-width: 1060px; margin: 0 auto; padding: 0 28px 110px; }
  .kicker {
    font-family: "IBM Plex Mono", monospace; font-size: 11px; font-weight: 500;
    letter-spacing: 0.2em; text-transform: uppercase; color: var(--accent);
  }
  header.masthead {
    padding: 78px 0 34px; display: grid; grid-template-columns: 216px minmax(0, 1fr);
    gap: 52px; align-items: center;
  }
  @media (max-width: 780px) { header.masthead { grid-template-columns: minmax(0, 1fr); gap: 30px; } }
  .hero { width: 216px; height: 216px; border-radius: 28%; box-shadow: 0 22px 52px -16px rgba(0,0,0,0.5); }
  h1 {
    font-family: Sora, system-ui, sans-serif; font-weight: 200;
    font-size: clamp(34px, 5.4vw, 54px); line-height: 1.02; letter-spacing: -0.025em;
    margin: 14px 0 0; text-wrap: balance;
  }
  .lede { margin: 18px 0 0; max-width: 58ch; color: var(--muted); text-wrap: pretty; }
  h2 {
    font-family: Sora, system-ui, sans-serif; font-weight: 400; font-size: 27px;
    line-height: 1.15; letter-spacing: -0.02em; margin: 62px 0 16px;
    padding-top: 26px; border-top: 1px solid var(--line); text-wrap: balance;
  }
  p { margin: 0 0 16px; max-width: 66ch; }
  .muted { color: var(--muted); }
  strong { font-weight: 500; }
  code {
    font-family: "IBM Plex Mono", monospace; font-size: 0.82em;
    background: var(--code-bg); padding: 2px 5px; border-radius: 4px;
  }
  .strip {
    margin: 4px 0 26px; padding: 26px 24px; background: var(--panel);
    border: 1px solid var(--line); border-radius: 14px;
    display: flex; flex-wrap: wrap; gap: 28px; align-items: flex-end;
  }
  .cap {
    font-family: "IBM Plex Mono", monospace; font-size: 9px; letter-spacing: 0.13em;
    text-transform: uppercase; color: var(--faint); text-align: center;
  }
  .tw { overflow-x: auto; margin: 0 0 26px; border: 1px solid var(--line); border-radius: 12px; }
  table {
    width: 100%; border-collapse: collapse; font-family: Sora, system-ui, sans-serif;
    font-size: 13.5px; line-height: 1.5;
  }
  thead th {
    text-align: left; font-weight: 600; font-size: 10.5px; letter-spacing: 0.1em;
    text-transform: uppercase; color: var(--faint); padding: 12px 15px;
    background: var(--panel); border-bottom: 1px solid var(--line); white-space: nowrap;
  }
  td { padding: 12px 15px; border-bottom: 1px solid var(--line); vertical-align: top; color: var(--muted); }
  tbody tr:last-child td { border-bottom: none; }
  td:first-child { color: var(--ink); white-space: nowrap; }
  ul { padding-left: 22px; max-width: 66ch; color: var(--muted); }
  li { margin-bottom: 10px; }
  .note {
    margin: 0 0 24px; padding: 20px 22px; background: var(--accent-soft);
    border-radius: 12px; font-size: 15.5px; color: var(--muted); max-width: 70ch;
  }
  .note strong { color: var(--ink); }
</style>

<div class="shell">
<header class="masthead">
  <div class="hero">@@HERO@@</div>
  <div>
    <div class="kicker">Sleep Noise · icono aprobado</div>
    <h1>Luna de grano</h1>
    <p class="lede">La luna dice «dormir» sin explicaciones, y aquí no está dibujada: está compuesta de @@N@@ puntos de grano. La forma la da la categoría; la textura, el producto. Elegida entre cuatro propuestas el 24 de agosto de 2026.</p>
  </div>
</header>

<h2>Las pruebas que tiene que pasar</h2>
<p class="muted">Un icono de Android no se juzga en grande. El sistema compone en 108 dp y solo muestra los 72 centrales, con una máscara que cambia según el lanzador. El círculo ámbar punteado del último cuadro es la zona segura de 66 dp: lo que se salga, se recorta en algún dispositivo.</p>
<div class="strip">@@STRIP@@</div>

<h2>El monocromo era el pendiente, y así se resuelve</h2>
<div class="note">
  <strong>El grano fino desaparece en una sola tinta.</strong> Android 13 y posteriores piden una capa
  monocroma para los iconos temáticos, y ahí no hay color: solo silueta. Con los @@N@@ puntos de 0,5 a
  1,65 dp, la capa monocroma quedaba en nada a tamaño pequeño — que es exactamente el defecto que le
  anoté a esta propuesta cuando era una de cuatro.
</div>
<p>La solución no es rendirse a una luna lisa. El creciente pasa a ser <strong>sólido</strong>, que garantiza
que se lea a cualquier tamaño, y la textura se traslada a <strong>@@RIM@@ puntos gruesos pegados al borde
exterior</strong>, de 1,2 a 2,2 dp. En grande se ve el grano; en pequeño se compacta en un contorno
ligeramente irregular. Sigue siendo la misma marca, y no es la luna genérica de la que huíamos.</p>

<h2>La familia completa</h2>
<p class="muted">Cuatro piezas, no una. Las dos primeras se superponen para formar el icono del lanzador; la tercera es la que usa el sistema con los iconos temáticos activados; la cuarta es un dibujo aparte.</p>
<div class="strip">@@FAMILY@@</div>
<p>El icono de notificación es el que más se olvida y el que más se ve: estará en la barra de estado
toda la noche mientras suena el ruido. A 24 dp el grano no existe, así que es el creciente en silueta,
sin fondo, para que el sistema lo tinte según el tema del usuario.</p>

<h2>Los ficheros, y dónde van</h2>
<div class="tw"><table>
<thead><tr><th>Fichero</th><th>Destino en el proyecto</th><th>Qué es</th></tr></thead>
<tbody>
<tr><td><code>ic_launcher.xml</code></td><td><code>res/mipmap-anydpi-v26/</code></td><td>El icono adaptativo, que referencia a las tres capas</td></tr>
<tr><td><code>ic_launcher_background.xml</code></td><td><code>res/drawable/</code></td><td>Viñeteado radial cálido, a sangre en los 108 dp</td></tr>
<tr><td><code>ic_launcher_foreground.xml</code></td><td><code>res/drawable/</code></td><td>El grano del creciente, @@N@@ puntos en un solo path</td></tr>
<tr><td><code>ic_launcher_monochrome.xml</code></td><td><code>res/drawable/</code></td><td>Creciente sólido con el borde granulado</td></tr>
<tr><td><code>ic_stat_sleep_noise.xml</code></td><td><code>res/drawable/</code></td><td>Icono de la barra de estado, 24 dp</td></tr>
<tr><td><code>play-icon-512.svg</code></td><td>—</td><td>Origen del PNG de 512×512 de la ficha de Play</td></tr>
</tbody>
</table></div>
<p class="muted">Todos salen de <code>scripts/generar-iconos.py</code>, que calcula el creciente por intersección
de circunferencias y siembra el grano con un generador determinista. No se editan a mano: se regeneran.
El grano va como puntos vectoriales y no como un filtro porque un <code>VectorDrawable</code> de Android
no soporta filtros — lo que se ve aquí es exactamente lo que se puede exportar.</p>

<h2>Lo que queda</h2>
<ul>
  <li><strong>Rasterizar el PNG de 512 × 512</strong> para la ficha de Play, desde el SVG. Play aplica su propia máscara, así que el fichero va con esquinas cuadradas.</li>
  <li><strong>Verlo en un dispositivo real</strong>, en H8: los lanzadores de Samsung y Xiaomi aplican máscaras propias y escalan distinto de lo que hace el emulador.</li>
  <li><strong>Comprobar el icono de notificación en la barra de estado</strong>, sobre fondo claro y oscuro, con el móvil a un brazo de distancia. Es la prueba que ninguna previsualización sustituye.</li>
</ul>
</div>
"""
for token, value in [("@@HERO@@", masked(216, "squircle")), ("@@STRIP@@", STRIP),
                     ("@@FAMILY@@", FAMILY), ("@@N@@", str(len(GRAIN))),
                     ("@@RIM@@", str(len(RIM)))]:
    page = page.replace(token, value)

(ROOT / "docs/design/iconos-sleep-noise.html").write_text(page)

assert page.count("<h1>") == 1
for tag in ("div", "table", "tbody", "ul", "p", "h2"):
    assert page.count("<" + tag + ">") + page.count("<" + tag + " ") == page.count("</" + tag + ">"), tag
print("grano:", len(GRAIN), "puntos | borde:", len(RIM), "puntos")
print("página:", len(page), "bytes | XML:", len(list(VEC.glob("*.xml"))), "ficheros")
