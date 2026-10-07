"""Index navigation and caption locators for the supplied OpenStax biomechanics PDF.

Run from the repository root. This is a book-specific metadata adapter, not a
content extractor or asset-clearance tool. --write refreshes generated files only.
"""

from __future__ import annotations

import argparse
import json
import re
from collections import Counter
from pathlib import Path

from pypdf import PdfReader

from content_common import sha256_file, verify_source, verify_page_count

FILENAME = "Biomechanics-of-Human-Movement-1600891203._print.pdf"
PDF = Path("theory-and-knowledge/knowledge/textbooks") / FILENAME
OUTPUT = Path("theory-and-knowledge/knowledge/textbook-indices/biomechanics-of-human-movement")
PDF_LINK = "../../textbooks/" + FILENAME
CHAPTER = re.compile(r"^Chapter\s+(\d+):\s*(.+)$")
SECTION = re.compile(r"^(\d+(?:\.\d+)*)(?:\.)?\s+(.+?)\s*$")
CAPTION = re.compile(r"^[ \t]*(Figure|Table)[ \t]+(\d+)\.[ \t]*(.*)$", re.MULTILINE)
CNX_URL = "https://cnx.org/contents/Ax2o07Ul@9.73:HR_VN3f7@3/Introduction-to-Science-and-th"
ATTRIBUTION = (
    "Source locator metadata for Karine Hamm, *Biomechanics of Human Movement* "
    "(title page PDF 3; 2016). The PDF's license notice (PDF 4–5) separately names "
    "OpenStax, *College Physics* and gives a CNX attribution URL; see the unresolved "
    "source-provenance note in `README.md`. CC BY 4.0, except where otherwise noted."
)


def normalized(text: str) -> str:
    return " ".join(text.split())


def bookmarks(reader: PdfReader) -> list[dict]:
    result: list[dict] = []

    def walk(nodes: list, depth: int = 0) -> None:
        for node in nodes:
            if isinstance(node, list):
                walk(node, depth + 1)
            else:
                page = reader.get_destination_page_number(node)
                if page is None or not 0 <= page < len(reader.pages):
                    raise ValueError(f"Invalid bookmark destination: {node.title}")
                result.append({"title": normalized(node.title), "pdf_start": page + 1, "depth": depth})

    walk(reader.outline)
    return result


def printed_footer(text: str) -> int | None:
    """Read the source's page number from a footer line separated by a vertical bar."""
    lines = [normalized(line) for line in text.splitlines() if line.strip()]
    found: list[int] = []
    for line in lines[-4:]:
        leading = re.match(r"^(\d{1,4})\s*\|\s*\S", line)
        trailing = re.match(r"^.+\|\s*(\d{1,4})$", line)
        if leading:
            found.append(int(leading.group(1)))
        if trailing:
            found.append(int(trailing.group(1)))
    values = set(found)
    return next(iter(values)) if len(values) == 1 else None


def add_spans(entries: list[dict], end: int, offset: int) -> list[dict]:
    """Keep shared heading pages in adjacent navigation ranges; not extraction bounds."""
    for index, entry in enumerate(entries):
        entry["pdf_end"] = entries[index + 1]["pdf_start"] if index + 1 < len(entries) else end
        entry["printed_start"] = entry["pdf_start"] - offset
        entry["printed_end"] = entry["pdf_end"] - offset
        entry["boundary_review_required"] = True
        if not 1 <= entry["pdf_start"] <= entry["pdf_end"] <= end:
            raise ValueError(f"Unordered or out-of-range heading: {entry}")
    return entries


def parse_captions(text: str, pdf_page: int, printed_page: int | None) -> list[dict]:
    """Create bounded caption-line candidates; page-based IDs avoid repeated figure numbers."""
    result: list[dict] = []
    per_kind: Counter[str] = Counter()
    for match in CAPTION.finditer(text):
        kind, number, lead = match.groups()
        per_kind[kind.lower()] += 1
        lead = normalized(lead)
        result.append({
            "id": f"bhm-{kind.lower()}-pdf{pdf_page}-{per_kind[kind.lower()]}",
            "kind": kind.lower(),
            "number": number,
            "pdf_page": pdf_page,
            "printed_page": printed_page,
            "caption_lead": lead[:110] + ("…" if len(lead) > 110 else ""),
            "status": "caption_line_candidate_visual_review_required",
            "rights_status": "item_specific_credit_and_exception_review_required",
        })
    return result


def build_index(pdf: Path) -> dict:
    reader = PdfReader(pdf)
    if reader.is_encrypted:
        raise ValueError("Encrypted PDF: inspect locally; this indexer does not request passwords.")
    outline = bookmarks(reader)
    top = [entry for entry in outline if entry["depth"] == 0]
    chapter_heads = [entry for entry in top if CHAPTER.fullmatch(entry["title"])]
    if [int(CHAPTER.fullmatch(entry["title"]).group(1)) for entry in chapter_heads] != list(range(1, 11)):
        raise ValueError("Expected ordered Chapters 1–10; inspect this PDF edition before indexing.")

    contents = next((entry for entry in top if entry["title"] == "Contents"), None)
    intro = next((entry for entry in top if entry["title"] == "Introduction to Open Textbooks"), None)
    if contents is None or intro is None:
        raise ValueError("Expected Contents and Introduction to Open Textbooks bookmarks.")

    page_text: list[str] = []
    footer_records: list[tuple[int, int]] = []
    empty_pages: list[int] = []
    for pdf_page, page in enumerate(reader.pages, 1):
        text = page.extract_text() or ""
        page_text.append(text)
        if not text.strip():
            empty_pages.append(pdf_page)
        printed = printed_footer(text)
        if printed is not None:
            footer_records.append((pdf_page, printed))

    offsets = Counter(pdf_page - printed for pdf_page, printed in footer_records)
    if not footer_records:
        raise ValueError("No numbered page footers found; verify this edition before indexing.")
    offset, matches = offsets.most_common(1)[0]
    mismatches = [(pdf_page, printed) for pdf_page, printed in footer_records if pdf_page - printed != offset]
    if mismatches:
        raise ValueError(f"Inconsistent printed-page mapping; first mismatches: {mismatches[:8]}")
    if matches < 700:
        raise ValueError(f"Insufficient footer evidence for a full-book map: {matches} pages.")

    chapters: list[dict] = []
    for chapter_index, head in enumerate(chapter_heads):
        match = CHAPTER.fullmatch(head["title"])
        number, title = int(match.group(1)), match.group(2)
        end = chapter_heads[chapter_index + 1]["pdf_start"] - 1 if chapter_index + 1 < len(chapter_heads) else len(reader.pages)
        outline_position = outline.index(head)
        children = []
        for entry in outline[outline_position + 1:]:
            if entry["depth"] == 0:
                break
            if entry["depth"] != 1:
                continue
            section = SECTION.fullmatch(entry["title"])
            children.append({
                "id": section.group(1) if section else entry["title"].casefold().replace(" ", "-"),
                "title": entry["title"],
                "kind": "section" if section else "navigation",
                "pdf_start": entry["pdf_start"],
                "location_evidence": "pdf_bookmark",
            })
        chapters.append({
            "number": number,
            "title": title,
            "pdf_start": head["pdf_start"],
            "pdf_end": end,
            "printed_start": head["pdf_start"] - offset,
            "printed_end": end - offset,
            "entries": add_spans(children, end, offset),
        })

    navigation = [
        {"title": "Cover and title pages", "pdf_start": 1, "pdf_end": 3, "printed_start": None, "printed_end": None},
        {"title": "License and attribution notice", "pdf_start": 4, "pdf_end": 5, "printed_start": None, "printed_end": None},
        {"title": "Contents", "pdf_start": contents["pdf_start"], "pdf_end": intro["pdf_start"] - 1, "printed_start": None, "printed_end": None},
        {"title": intro["title"], "pdf_start": intro["pdf_start"], "pdf_end": chapter_heads[0]["pdf_start"] - 1, "printed_start": None, "printed_end": None},
    ]

    graphics: list[dict] = []
    chapter_by_page = [chapter for chapter in chapters]
    for pdf_page in range(chapter_heads[0]["pdf_start"], len(reader.pages) + 1):
        text = page_text[pdf_page - 1]
        printed = pdf_page - offset if any(page == pdf_page for page, _ in footer_records) else None
        candidates = parse_captions(text, pdf_page, printed)
        chapter = next(c for c in chapter_by_page if c["pdf_start"] <= pdf_page <= c["pdf_end"])
        for graphic in candidates:
            graphic["chapter"] = chapter["number"]
            graphic["section_candidates"] = [
                entry["id"] for entry in chapter["entries"]
                if entry["kind"] == "section" and entry["pdf_start"] <= pdf_page <= entry["pdf_end"]
            ]
            graphics.append(graphic)

    source = {
        "id": "biomechanics-of-human-movement",
        "filename": FILENAME,
        "title": str(reader.metadata.title or "Biomechanics of Human Movement"),
        "authors": ["Karine Hamm"],
        "publication_year": 2016,
        "pdf_pages": len(reader.pages),
        "sha256": sha256_file(pdf),
        "pdf_creation_date": str(reader.metadata.get("/CreationDate", "")),
        "citation": "Karine Hamm, Biomechanics of Human Movement (2016; title-page attribution).",
        "source_url": CNX_URL,
        "source_url_status": "Transcribed from the PDF license notice; appears incomplete and names a different source title. Verify before reuse.",
        "license": "CC BY 4.0, except where otherwise noted; see item-specific credits and exceptions.",
        "license_url": "https://creativecommons.org/licenses/by/4.0/",
        "required_attribution": "The PDF license notice names OpenStax, College Physics and specifies a CNX download attribution; verify the exact source URL and attribution before redistribution.",
        "license_notice_pdf_pages": [4, 5],
        "printed_page_offset": offset,
        "page_mapping_evidence": {
            "method": "Printed numbers read from footer lines on every extractable page; offset derived from and consistent across recognized footers.",
            "verified_footer_count": len(footer_records),
            "first_verified_pdf_page": footer_records[0][0],
            "last_verified_pdf_page": footer_records[-1][0],
            "unmapped_pdf_pages_within_content": [page for page in range(chapter_heads[0]["pdf_start"], len(reader.pages) + 1) if not any(p == page for p, _ in footer_records)],
            "pdf_page_labels_are_printed_numbers": False,
        },
    }
    return {
        "schema_version": 1,
        "source": source,
        "coverage": {
            "status": "full navigation and caption-line locator scan; human source, domain, editorial and rights review remain separate",
            "all_pdf_pages_scanned_for_text_and_footer": True,
            "empty_text_pdf_pages": empty_pages,
            "chapter_count": len(chapters),
            "numbered_section_count": sum(entry["kind"] == "section" for chapter in chapters for entry in chapter["entries"]),
            "caption_line_candidate_count": len(graphics),
            "graphics_method": "Text lines beginning Figure N. or Table N.; page-based IDs prevent collisions when book numbering repeats. Caption lines are candidates, not verified titles or complete captions.",
            "unlabelled_artwork": "Not indexed; discover during rendered-page review.",
            "standalone_glossary_index_references_appendices_and_answer_keys": "Not fully audited in this indexing pass; check the source before relying on their presence or absence.",
        },
        "navigation": navigation,
        "chapters": chapters,
        "graphics": graphics,
    }


def pdf_link(first: int, last: int | None = None) -> str:
    label = str(first) if last is None or last == first else f"{first}–{last}"
    return f"[{label}]({PDF_LINK}#page={first})"


def cell(text: str) -> str:
    return text.replace("|", "\\|").replace("\n", " ")


def render_chapters(index: dict) -> str:
    source = index["source"]
    lines = [
        "# Biomechanics of Human Movement — Chapter and Section Index", "", ATTRIBUTION, "",
        "> Generated by `tools/index_biomechanics.py --write`; do not edit this file manually.",
        "> Heading spans include the next heading page to preserve shared-page boundaries; they are not approved extraction ranges.", "",
        f"Local edition fingerprint: `{source['sha256']}`; {source['pdf_pages']} PDF pages.",
        f"Printed page + {source['printed_page_offset']} = PDF viewer page, checked against {source['page_mapping_evidence']['verified_footer_count']} recognized footers.",
        "PDF page labels are viewer numbers. Six content pages have no recognized printed footer; see `index.json`.", "",
        "## Book Navigation", "", "| Aid | PDF viewer pages | Printed pages |", "|---|---|---|",
    ]
    for entry in index["navigation"]:
        lines.append(f"| {entry['title']} | {pdf_link(entry['pdf_start'], entry['pdf_end'])} | unnumbered / unmapped |")
    lines += ["", "License/attribution notice: " + pdf_link(4, 5) + ". The notice cites OpenStax, *College Physics*, while the title page names *Biomechanics of Human Movement*; see the open question in `README.md`.", "", "## Chapter Directory", "", "| Chapter | PDF viewer pages | Printed pages |", "|---|---|---|"]
    for chapter in index["chapters"]:
        lines.append(f"| {chapter['number']}. {cell(chapter['title'])} | {pdf_link(chapter['pdf_start'], chapter['pdf_end'])} | {chapter['printed_start']}–{chapter['printed_end']} |")
    for chapter in index["chapters"]:
        lines += ["", f"## Chapter {chapter['number']}: {chapter['title']}", "", "| Section / navigation bookmark | PDF navigation span | Printed span |", "|---|---|---|"]
        for entry in chapter["entries"]:
            lines.append(f"| {cell(entry['title'])} | {pdf_link(entry['pdf_start'], entry['pdf_end'])} | {entry['printed_start']}–{entry['printed_end']} |")
    return "\n".join(lines) + "\n"


def render_graphics(index: dict) -> str:
    lines = [
        "# Biomechanics of Human Movement — Figure and Table Locator Candidates", "", ATTRIBUTION, "",
        "> Generated caption-line candidates, not a visually reviewed or cleared asset library.",
        "> A caption page may differ from artwork pages. Inspect the complete caption, panels, labels and credits before any reuse.",
        "> Page-based stable IDs are used because figure/table numbers repeat in the source.", "",
    ]
    for chapter in index["chapters"]:
        records = [graphic for graphic in index["graphics"] if graphic["chapter"] == chapter["number"]]
        lines += [f"## Chapter {chapter['number']}: {chapter['title']}", "", "| Stable ID | Label | Caption lead (truncated) | PDF caption page | Printed | Section candidates | Status |", "|---|---|---|---|---|---|---|"]
        for graphic in records:
            printed = str(graphic["printed_page"]) if graphic["printed_page"] is not None else "unmapped"
            lead = cell(graphic["caption_lead"]) or "[No lead captured; inspect source]"
            sections = ", ".join(graphic["section_candidates"]) or "unresolved"
            lines.append(f"| `{graphic['id']}` | {graphic['kind'].title()} {graphic['number']} | {lead} | {pdf_link(graphic['pdf_page'])} | {printed} | {sections} | visual/rights review required |")
    return "\n".join(lines) + "\n"


def lookup(index: dict, query: str) -> list[dict]:
    needle = query.casefold()
    records = []
    for chapter in index["chapters"]:
        records.append({"chapter": chapter["number"], "title": chapter["title"], "pdf_start": chapter["pdf_start"], "pdf_end": chapter["pdf_end"]})
        records.extend({"chapter": chapter["number"], **entry} for entry in chapter["entries"])
    records.extend(index["graphics"])
    records.extend(index["navigation"])
    return [record for record in records if needle in json.dumps(record, ensure_ascii=False).casefold()]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--pdf", type=Path, default=PDF)
    parser.add_argument("--output-dir", type=Path, default=OUTPUT)
    parser.add_argument("--write", action="store_true", help="Refresh generated index files only.")
    parser.add_argument("--query", help="Search the saved metadata without opening the PDF.")
    parser.add_argument("--check-source", action="store_true", help="Verify the saved index fingerprint and page count.")
    args = parser.parse_args()
    root = args.repo_root.resolve()
    output = root / args.output_dir
    pdf = root / args.pdf
    try:
        if args.query is not None or args.check_source:
            index = json.loads((output / "index.json").read_text(encoding="utf-8"))
            if args.check_source:
                source = verify_source(pdf, index)
                reader = PdfReader(pdf)
                verify_page_count(len(reader.pages), source)
                print("Source fingerprint and page count match the index.")
            if args.query is not None:
                print(json.dumps(lookup(index, args.query), ensure_ascii=False, indent=2))
            return 0
        index = build_index(pdf)
    except (OSError, ValueError, KeyError) as error:
        parser.error(str(error))
    print(f"Validated {len(index['chapters'])} chapters, {index['coverage']['numbered_section_count']} numbered sections, {len(index['graphics'])} figure/table caption-line candidates.")
    print(f"Verified {index['source']['page_mapping_evidence']['verified_footer_count']} printed footers; SHA-256 {index['source']['sha256']}.")
    if args.write:
        output.mkdir(parents=True, exist_ok=True)
        (output / "index.json").write_text(json.dumps(index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        (output / "chapters.md").write_text(render_chapters(index), encoding="utf-8")
        (output / "graphics.md").write_text(render_graphics(index), encoding="utf-8")
        print(f"Refreshed generated files in {output}; human-owned records left untouched.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
