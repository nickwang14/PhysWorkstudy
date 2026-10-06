"""Read up to twelve indexed local PDF pages to stdout; never write source dumps.

Run from any directory; relative paths are resolved against this repository root.
This is a review aid, not a publication/export pipeline or a rights clearance.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path

from pypdf import PdfReader

ROOT = Path(__file__).resolve().parents[4]
DEFAULT_PDF = Path("docs/anatomy-and-physiology-2e_-_WEB.pdf")
DEFAULT_INDEX = Path("theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e/index.json")
MAX_PAGES = 12


def parse_pages(spec: str, page_count: int) -> list[int]:
    pages: set[int] = set()
    for part in spec.split(","):
        match = re.fullmatch(r"\s*([1-9]\d*)(?:\s*-\s*([1-9]\d*))?\s*", part)
        if not match:
            raise ValueError("Use one-based PDF pages, for example 350 or 346-350,354.")
        first = int(match.group(1))
        last = int(match.group(2) or first)
        if not 1 <= first <= last <= page_count:
            raise ValueError(f"Invalid PDF span {first}-{last}; source has {page_count} pages.")
        if last - first + 1 > MAX_PAGES:
            raise ValueError(f"Read at most {MAX_PAGES} distinct pages per invocation.")
        pages.update(range(first, last + 1))
        if len(pages) > MAX_PAGES:
            raise ValueError(f"Read at most {MAX_PAGES} distinct pages per invocation.")
    return sorted(pages)


def verify_source(pdf: Path, index: dict) -> dict:
    source = index["source"]
    if hashlib.sha256(pdf.read_bytes()).hexdigest() != source["sha256"]:
        raise ValueError("PDF fingerprint differs from the index. Regenerate and revalidate references first.")
    return source


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pdf", type=Path, default=DEFAULT_PDF)
    parser.add_argument("--index", type=Path, default=DEFAULT_INDEX)
    parser.add_argument("--pdf-pages", required=True, help="Inclusive one-based viewer pages: 350 or 346-350,354.")
    parser.add_argument("--layout", action="store_true", help="Preserve text layout for manual reading-order review.")
    parser.add_argument("--metadata-only", action="store_true", help="Validate selection/fingerprint without printing source text.")
    args = parser.parse_args()
    try:
        pdf = ROOT / args.pdf
        index = json.loads((ROOT / args.index).read_text(encoding="utf-8"))
        source = verify_source(pdf, index)
        reader = PdfReader(pdf)
        if reader.is_encrypted:
            raise ValueError("Encrypted PDF: inspect it locally; this reader does not request passwords.")
        if len(reader.pages) != source["pdf_pages"]:
            raise ValueError("Page count differs from the index.")
        pages = parse_pages(args.pdf_pages, len(reader.pages))
    except (OSError, ValueError, KeyError) as error:
        parser.error(str(error))
    print(f"Source: {source['citation']}")
    print(f"File: {pdf.name}; SHA-256: {source['sha256']}")
    print(f"Source URL: {source['source_url']}")
    print(f"License: {source['license']}; {source['license_url']}")
    print(f"Required attribution: {source['required_attribution']}")
    print("Status: machine_extracted_review_required; raw text, not an approved learner extract.")
    print("No files/images exported. Verify rendered pages, partial boundaries and item-specific rights before reuse.")
    offset = source.get("printed_page_offset")
    for number in pages:
        # Unnumbered front matter must not receive invented zero/negative printed pages.
        printed = number - offset if isinstance(offset, int) and number > offset else "unmapped/front matter"
        print(f"\n### PDF page {number}; printed page {printed}")
        if not args.metadata_only:
            text = reader.pages[number - 1].extract_text(extraction_mode="layout" if args.layout else "plain") or ""
            print(text if text.strip() else "[No extractable text: inspect the rendered page; OCR may be needed.]")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())