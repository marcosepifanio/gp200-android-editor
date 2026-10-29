"""Gera as imagens da ficha da Play Store a partir das capturas em docs/images.

Uso (na raiz do projeto): python3 docs/play-store/make_graphics.py
Requer Pillow e as fontes Noto Sans instaladas no sistema.
"""
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs/play-store/graphics"
ICON = ROOT / "app/src/main/res/drawable-nodpi/app_icon_preview.png"
FONT_BOLD = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"
FONT_REGULAR = "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"

# Cores de StudioPalette.
CASE = (20, 21, 23)
SURFACE = (36, 38, 42)
ACCENT = (79, 214, 238)
TEXT_PRIMARY = (236, 234, 229)
TEXT_MUTED = (152, 147, 138)

# Capturas sem marcas de terceiros, com título e subtítulo.
SCREENSHOTS = [
    ("conexao", "Conecte por USB", "Ou explore o app no modo offline"),
    ("cadeia", "Sua cadeia de sinal", "Ative e reordene os blocos do preset"),
    ("editor", "Ajuste cada efeito", "Parâmetros com os limites do catálogo"),
    ("presets", "Organize os presets", "Slots, importação e exportação .prst"),
    ("controles-ctrls", "Knobs e CTRL", "Defina o que cada controle físico faz"),
    ("ritmos", "Ritmos", "Padrões, volume e andamento"),
]


def vertical_gradient(size, top, bottom):
    w, h = size
    img = Image.new("RGB", size)
    draw = ImageDraw.Draw(img)
    for y in range(h):
        t = y / max(h - 1, 1)
        draw.line([(0, y), (w, y)], fill=tuple(round(a + (b - a) * t) for a, b in zip(top, bottom)))
    return img


def rounded(img, radius):
    mask = Image.new("L", img.size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([(0, 0), img.size], radius=radius, fill=255)
    out = img.convert("RGBA")
    out.putalpha(mask)
    return out


def paste_with_shadow(canvas, img, pos, radius, blur=28):
    shadow = Image.new("RGBA", (img.width + blur * 4, img.height + blur * 4), (0, 0, 0, 0))
    ImageDraw.Draw(shadow).rounded_rectangle(
        [(blur * 2, blur * 2), (blur * 2 + img.width, blur * 2 + img.height)], radius=radius, fill=(0, 0, 0, 170)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(blur))
    canvas.alpha_composite(shadow, (pos[0] - blur * 2, pos[1] - blur * 2 + 12))
    canvas.alpha_composite(img, pos)


def centered_text(draw, y, text, font, fill, width):
    w = draw.textlength(text, font=font)
    draw.text(((width - w) / 2, y), text, font=font, fill=fill)


def screenshot(name, title, subtitle, index):
    w, h = 1080, 1920
    canvas = vertical_gradient((w, h), SURFACE, CASE).convert("RGBA")
    draw = ImageDraw.Draw(canvas)
    centered_text(draw, 92, title, ImageFont.truetype(FONT_BOLD, 76), TEXT_PRIMARY, w)
    centered_text(draw, 196, subtitle, ImageFont.truetype(FONT_REGULAR, 40), TEXT_MUTED, w)
    draw.rounded_rectangle([(w / 2 - 48, 270), (w / 2 + 48, 276)], radius=3, fill=ACCENT)

    shot = Image.open(ROOT / f"docs/images/{name}.png").convert("RGB")
    shot_h = 1560
    shot_w = round(shot.width * shot_h / shot.height)
    shot = rounded(shot.resize((shot_w, shot_h), Image.LANCZOS), 44)
    paste_with_shadow(canvas, shot, ((w - shot_w) // 2, h - shot_h - 40), 44)
    canvas.convert("RGB").save(OUT / f"phone-{index:02d}-{name}.png", optimize=True)


def feature_graphic():
    w, h = 1024, 500
    canvas = vertical_gradient((w, h), SURFACE, CASE).convert("RGBA")
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    ImageDraw.Draw(glow).ellipse([(40, 60), (420, 440)], fill=ACCENT + (70,))
    canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(70)))

    icon = Image.open(ICON).convert("RGBA").resize((300, 300), Image.LANCZOS)
    canvas.alpha_composite(icon, (80, 100))

    draw = ImageDraw.Draw(canvas)
    x = 430
    draw.text((x, 132), "Editor para", font=ImageFont.truetype(FONT_REGULAR, 44), fill=TEXT_MUTED)
    draw.text((x, 178), "GP-200", font=ImageFont.truetype(FONT_BOLD, 104), fill=TEXT_PRIMARY)
    draw.rounded_rectangle([(x + 4, 316), (x + 84, 322)], radius=3, fill=ACCENT)
    draw.text((x, 340), "Edite presets da pedaleira pelo celular", font=ImageFont.truetype(FONT_REGULAR, 30), fill=TEXT_PRIMARY)
    draw.text((x, 384), "App independente e não oficial", font=ImageFont.truetype(FONT_REGULAR, 24), fill=TEXT_MUTED)
    canvas.convert("RGB").save(OUT / "feature-graphic.png", optimize=True)


def icon_512():
    Image.open(ICON).convert("RGBA").resize((512, 512), Image.LANCZOS).save(OUT / "icon-512.png", optimize=True)


if __name__ == "__main__":
    OUT.mkdir(parents=True, exist_ok=True)
    icon_512()
    feature_graphic()
    for i, (name, title, subtitle) in enumerate(SCREENSHOTS, start=1):
        screenshot(name, title, subtitle, i)
    for p in sorted(OUT.iterdir()):
        with Image.open(p) as im:
            print(f"{p.relative_to(ROOT)}: {im.size[0]}x{im.size[1]} {p.stat().st_size // 1024} KB")
