"""Shared paths and validation for content tools; no platform-specific imports.

Keep book-specific caption/TOC parsing in its adapter. Text and image page limits
are intentionally different. Flat frontmatter parsing is not a general YAML parser.
"""

from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[1]
CURRICULUM = Path("theory-and-knowledge/knowledge/curriculum")
AP_FILENAME = "anatomy-and-physiology-2e_-_WEB.pdf"
AP_OFFSET = 16
AP_INDEX_DIR = Path("theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e")
AP_INDEX = AP_INDEX_DIR / "index.json"
AP_PDF = Path("docs") / AP_FILENAME
READ_MORE = re.compile(r"^## Read More \(Optional\)\s*(.*?)(?=^## |\Z)", re.MULTILINE | re.DOTALL)


def read_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8-sig"))


def metadata(text: str) -> dict[str, str]:
    """Parse the repository's flat scalar reference fields, accepting UTF-8 BOM."""
    text = text.removeprefix("\ufeff").replace("\r\n", "\n")
    match = re.match(r"\A---[ \t]*\n(.*?)\n---(?:\n|$)", text, re.DOTALL)
    if not match:
        raise ValueError("Missing frontmatter")
    result = {}
    for key, value in re.findall(r"^([\w-]+):[ \t]*(.*)$", match.group(1), re.MULTILINE):
        value = value.strip()
        if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
            value = value[1:-1]
        result[key] = value
    return result


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify_source(pdf: Path, index: dict) -> dict:
    source = index["source"]
    if sha256_file(pdf) != source["sha256"]:
        raise ValueError("PDF fingerprint differs from the index. Regenerate and revalidate references first.")
    return source


def verify_page_count(actual: int, source: dict) -> None:
    if actual != source["pdf_pages"]:
        raise ValueError("Page count differs from the index.")


def validate_page_span(first: int, last: int, page_count: int) -> None:
    if not 1 <= first <= last <= page_count:
        raise ValueError(f"Invalid PDF span {first}-{last}; source has {page_count} pages.")


def bounded_pages(pages, page_count: int, max_pages: int) -> list[int]:
    """Validate a deduplicated collection without changing its one-based meaning."""
    selected = sorted(set(pages))
    if not all(isinstance(page, int) and 1 <= page <= page_count for page in selected):
        raise ValueError("Review pages must be within the indexed PDF.")
    if len(selected) > max_pages:
        raise ValueError(f"Select at most {max_pages} distinct pages per invocation.")
    return selected


def parse_pages(spec: str, page_count: int, max_pages: int = 12) -> list[int]:
    pages: set[int] = set()
    for part in spec.split(","):
        match = re.fullmatch(r"\s*([1-9]\d*)(?:\s*-\s*([1-9]\d*))?\s*", part)
        if not match:
            raise ValueError("Use one-based PDF pages, for example 350 or 346-350,354.")
        first = int(match.group(1))
        last = int(match.group(2) or first)
        validate_page_span(first, last, page_count)
        if last - first + 1 > max_pages:
            raise ValueError(f"Select at most {max_pages} distinct pages per invocation.")
        pages.update(range(first, last + 1))
        if len(pages) > max_pages:
            raise ValueError(f"Select at most {max_pages} distinct pages per invocation.")
    return sorted(pages)