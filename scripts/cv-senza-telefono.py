"""Crea la versione pubblica del CV: rimuove DAVVERO il numero di telefono (redazione: il testo
sparisce dal file, non viene coperto) e riscrive la riga di contatto con stesso font, dimensione,
colore e posizione. Link, grafica e foto restano intatti.

Requisiti: pip install pymupdf   e il file .ttf del font usato dal CV (es. Lato-Regular.ttf).
Uso:       python cv-senza-telefono.py CV_originale.pdf web-public/src/main/resources/static/cv/Jeanfranco_Alvarado_CV.pdf Lato-Regular.ttf
"""
import sys

import pymupdf

src, dst, font_file = sys.argv[1:4]
doc = pymupdf.open(src)
page = doc[0]

span = next((s for b in page.get_text("dict")["blocks"] for l in b.get("lines", []) for s in l["spans"]
             if "+39" in s["text"]), None)
assert span, "riga del telefono non trovata"
x0, y0, x1, y1 = span["bbox"]
origin, size, c = span["origin"], span["size"], span["color"]
color = ((c >> 16) / 255, ((c >> 8) & 255) / 255, (c & 255) / 255)
parts = [p.strip() for p in span["text"].split("·")]
city, email = parts[0], parts[-1]

links_before = [l for l in page.get_links() if not l.get("uri", "").startswith("mailto:")]

# 1) redazione della sola riga: grafica (bordi dei bottoni) e immagini (foto) non vengono toccate
page.add_redact_annot(pymupdf.Rect(x0 - 1, y0 + 0.8, x1 + 1, y1 - 0.8))
page.apply_redactions(images=pymupdf.PDF_REDACT_IMAGE_NONE, graphics=pymupdf.PDF_REDACT_LINE_ART_NONE)

# la redazione cancella anche i link che toccano l'area: si rimettono quelli spariti
still = {l.get("uri") for l in page.get_links()}
for l in links_before:
    if l.get("uri") not in still:
        page.insert_link({"kind": pymupdf.LINK_URI, "uri": l["uri"], "from": l["from"]})

# 2) riga riscritta senza telefono
new_text = f"{city}  ·  {email}"
font = pymupdf.Font(fontfile=font_file)
page.insert_font(fontname="cvfont", fontfile=font_file)
page.insert_text(origin, new_text, fontname="cvfont", fontsize=size, color=color)

# 3) link mailto sopra la nuova posizione dell'email
for link in page.get_links():
    if link.get("uri", "").startswith("mailto:"):
        page.delete_link(link)
ex0 = origin[0] + font.text_length(new_text[: -len(email)], fontsize=size)
page.insert_link({"kind": pymupdf.LINK_URI, "uri": "mailto:" + email,
                  "from": pymupdf.Rect(ex0, y0 - 1, ex0 + font.text_length(email, fontsize=size), y1 - 0.5)})

doc.set_metadata({**doc.metadata, "title": "Jeanfranco Alvarado - CV"})
doc.save(dst, garbage=4, deflate=True)

# verifiche: nessuna traccia del numero, stessi link di prima
check = pymupdf.open(dst)
text = "".join(p.get_text() for p in check)
for needle in ("+39", "388", "808 2120"):
    assert needle not in text, f"ATTENZIONE: trovato ancora '{needle}'"
uris = sorted(l.get("uri") for l in check[0].get_links())
assert uris == sorted([l.get("uri") for l in links_before] + ["mailto:" + email]), f"link cambiati: {uris}"
print(f"OK: telefono rimosso, {len(uris)} link intatti -> {dst}")
