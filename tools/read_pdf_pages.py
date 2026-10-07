"""Read at most twelve indexed local PDF pages to stdout; export no files/images.

Relative paths resolve against the repository root, not the current directory.
This is a review aid, not a publication pipeline or rights clearance.
"""

from __future__ import annotations

import argparse
from pathlib import Path

from pypdf import PdfReader

from content_common import AP_INDEX, AP_PDF, REPO_ROOT, parse_pages, read_json, verify_page_count, verify_source

ROOT = REPO_ROOT
DEFAULT_PDF = AP_PDF
DEFAULT_INDEX = AP_INDEX
MAX_PAGES = 12


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pdf", type=Path, default=DEFAULT_PDF)
    parser.add_argument("--index", type=Path, default=DEFAULT_INDEX)
    parser.add_argument("--pdf-pages", required=True, help="Inclusive one-based viewer pages: 350 or 346-350,354.")
    parser.add_argument("--layout", action="store_true", help="Preserve layout for manual reading-order review.")
    parser.add_argument("--metadata-only", action="store_true", help="Validate without printing source text.")
    args = parser.parse_args()
    try:
        pdf = ROOT / args.pdf
        source = verify_source(pdf, read_json(ROOT / args.index))
        reader = PdfReader(pdf)
        if reader.is_encrypted:
            raise ValueError("Encrypted PDF: inspect locally; this reader does not request passwords.")
        verify_page_count(len(reader.pages), source)
        pages = parse_pages(args.pdf_pages, len(reader.pages), MAX_PAGES)
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
        printed = number - offset if isinstance(offset, int) and number > offset else "unmapped/front matter"
        print(f"\n### PDF page {number}; printed page {printed}")
        if not args.metadata_only:
            text = reader.pages[number - 1].extract_text(extraction_mode="layout" if args.layout else "plain") or ""
            print(text if text.strip() else "[No extractable text: inspect the rendered page; OCR may be needed.]")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())