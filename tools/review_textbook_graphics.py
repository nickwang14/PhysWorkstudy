"""Render selected indexed A&P graphic pages into an ignored local review cache.

Page previews/contact sheets are inspection aids, NOT extracted figures or approved
app assets. Human visual/credit review is recorded separately in the source catalogue.
"""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

import pypdfium2 as pdfium
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
INDEX = Path("theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/index.json")
MAX_PAGES = 32


def select_graphics(index: dict, ids: list[str]) -> list[dict]:
    by_id = {g["id"]: g for g in index["graphics"]}
    if not ids:
        raise ValueError("Select at least one stable graphic ID.")
    unknown = sorted(set(ids) - by_id.keys())
    if unknown:
        raise ValueError("Unknown graphic IDs: " + ", ".join(unknown))
    return [by_id[key] for key in dict.fromkeys(ids)]


def review_pages(graphics: list[dict], extra_pages: list[int], page_count: int) -> list[int]:
    pages = set(extra_pages)
    for g in graphics:
        pages.update(o["pdf_page"] for o in g["caption_occurrences"])
    if not all(1 <= page <= page_count for page in pages):
        raise ValueError("Review pages must be within the indexed PDF.")
    if len(pages) > MAX_PAGES:
        raise ValueError(f"Review at most {MAX_PAGES} pages in one batch.")
    return sorted(pages)


def contact_sheet(paths: list[Path], labels: list[str], destination: Path) -> None:
    """Four full pages per sheet; page PNGs retain the full rendering resolution."""
    images = []
    for path in paths:
        with Image.open(path) as image:
            images.append(image.convert("RGB"))
    width = max(i.width for i in images)
    height = max(i.height for i in images)
    header = 36
    columns = 2 if len(images) > 1 else 1
    rows = (len(images) + columns - 1) // columns
    sheet = Image.new("RGB", (columns * width, rows * (height + header)), "#e5e7eb")
    draw = ImageDraw.Draw(sheet)
    for pos, (image, label) in enumerate(zip(images, labels)):
        x = (pos % columns) * width
        y = (pos // columns) * (height + header)
        draw.text((x + 12, y + 10), label, fill="black")
        sheet.paste(image, (x, y + header))
        image.close()
    sheet.save(destination)
    sheet.close()


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ids", nargs="+", required=True, help="Stable IDs, e.g. ap-figure-9.8 ap-figure-9.10.")
    parser.add_argument("--extra-pages", type=int, nargs="*", default=[], help="Neighboring artwork/credit pages to inspect.")
    parser.add_argument("--dpi", type=int, default=120, help="Preview resolution, 72–180 dpi.")
    parser.add_argument("--dry-run", action="store_true", help="Check IDs, source and pages without writing images.")
    args = parser.parse_args()
    try:
        if not 72 <= args.dpi <= 180:
            raise ValueError("Use a review resolution between 72 and 180 dpi.")
        index = json.loads((ROOT / INDEX).read_text(encoding="utf-8"))
        source = index["source"]
        pdf = ROOT / "docs" / source["filename"]
        if hashlib.sha256(pdf.read_bytes()).hexdigest() != source["sha256"]:
            raise ValueError("PDF fingerprint changed. Regenerate/revalidate its index first.")
        graphics = select_graphics(index, args.ids)
        pages = review_pages(graphics, args.extra_pages, source["pdf_pages"])
    except (OSError, ValueError, KeyError) as error:
        parser.error(str(error))

    output = ROOT / "docs/graphics-review" / source["id"] / source["sha256"][:12]
    print(f"Selected {len(graphics)} graphics on {len(pages)} review pages: {pages}")
    print(f"Local-only cache: {output}")
    print("Rendering is not visual/rights clearance. Record actual review in graphic-review.md.")
    if args.dry_run:
        return 0

    output.mkdir(parents=True, exist_ok=True)
    previews: list[Path] = []
    labels: list[str] = []
    with pdfium.PdfDocument(str(pdf)) as document:
        if len(document) != source["pdf_pages"]:
            raise ValueError("Rendered page count differs from the indexed source.")
        for number in pages:
            page = document[number - 1]
            bitmap = page.render(scale=args.dpi / 72)
            image = bitmap.to_pil()
            path = output / f"pdf-{number:04d}.png"
            image.save(path)
            image.close()
            bitmap.close()
            page.close()
            previews.append(path)
            labels.append(f"PDF {number} | printed {number - source['printed_page_offset']}")

    sheets = []
    for first in range(0, len(previews), 4):
        path = output / f"contact-{first // 4 + 1:02d}.png"
        contact_sheet(previews[first:first + 4], labels[first:first + 4], path)
        sheets.append(path.name)
        print(path)
    manifest = {
        "source_id": source["id"], "source_sha256": source["sha256"],
        "source_citation": source["citation"], "license": source["license"],
        "license_url": source["license_url"], "required_attribution": source["required_attribution"],
        "graphic_ids": [g["id"] for g in graphics], "pdf_pages": pages,
        "preview_files": [p.name for p in previews], "contact_sheets": sheets, "dpi": args.dpi,
        "status": "local_review_previews_not_reuse_cleared",
        "visual_review": "not inferred from rendering; record human inspection separately",
    }
    (output / "manifest.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())