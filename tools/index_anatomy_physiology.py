"""Index the supplied A&P 2e PDF without copying its body text or artwork.

Run from the repository root. Default is validation only; --write refreshes only
index.json, chapters.md and graphics.md. Curated notes and usage logs are untouched.
This adapter uses this edition's bookmarks, uppercase captions and printed footers.
Other textbooks need their own verified adapter, not an assumed page offset.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import re
from pathlib import Path

from pypdf import PdfReader

FILENAME = "anatomy-and-physiology-2e_-_WEB.pdf"
OFFSET = 16
OUTPUT = Path("theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e")
CHAPTER = re.compile(r"^Chapter (\d+) (.+)$")
SECTION = re.compile(r"^(\d+\.\d+) (.+)$")
# Uppercase FIGURE/TABLE distinguishes caption labels from in-text references.
CAPTION = re.compile(r"^[ \t]*(FIGURE|TABLE)[ \t]+(\d+\.\d+)\b[ \t]*(.*)$", re.MULTILINE)
PDF_LINK = "../../../../docs/" + FILENAME
ATTRIBUTION = (
    "Source: J. Gordon Betts et al. / OpenStax, *Anatomy and Physiology 2e*, "
    "© 2026 Rice University; original publication 2022. "
    "[Source](https://openstax.org/details/books/anatomy-and-physiology-2e). "
    "[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/), "
    "except separately credited material. Access for free at openstax.org. "
    "These are navigational metadata, not reproduced textbook pages."
)


def normalized(text: str) -> str:
    return " ".join(text.split())


def printed_footer(text: str, pdf_page: int) -> int | None:
    """Recognize numbered book footers, never ordinary body-text numbers."""
    if pdf_page <= OFFSET:
        return None  # Contents entries (e.g. "References 1305") are not footers.
    for line in text.splitlines()[-5:]:
        line = line.strip()
        leading = re.match(r"^(\d+)\s+(?:\d+\s*•|References\b|Index\b|Preface\b)", line)
        trailing = re.search(r"(?:•.*|^References|^Index|^Preface)\s+(\d+)\s*$", line)
        if leading:
            return int(leading.group(1))
        if trailing:
            return int(trailing.group(1))
    return None


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


def add_spans(entries: list[dict], end: int) -> list[dict]:
    """Navigation spans include the next heading page to avoid losing shared text."""
    for i, entry in enumerate(entries):
        entry["pdf_end"] = entries[i + 1]["pdf_start"] if i + 1 < len(entries) else end
        entry["printed_start"] = entry["pdf_start"] - OFFSET
        entry["printed_end"] = entry["pdf_end"] - OFFSET
        entry["boundary_review_required"] = True
        if not 1 <= entry["pdf_start"] <= entry["pdf_end"] <= end:
            raise ValueError(f"Unordered or out-of-range heading: {entry}")
    return entries


def parse_captions(text: str, pdf_page: int) -> list[dict]:
    result: list[dict] = []
    for match in CAPTION.finditer(text):
        kind, number, lead = match.groups()
        # A short locator, NOT a reconstructed/full caption or a verified title.
        lead = normalized(lead)
        result.append({
            "id": f"ap-{kind.lower()}-{number}", "kind": kind.lower(), "number": number,
            "chapter": int(number.split(".")[0]), "pdf_page": pdf_page,
            "printed_page": pdf_page - OFFSET,
            "caption_lead": lead[:110] + ("…" if len(lead) > 110 else ""),
            "status": "caption_detected_visual_review_required",
            "rights_status": "figure_or_table_specific_credit_review_required",
        })
    return result


def merge_captions(graphics: list[dict]) -> list[dict]:
    """Keep repeated table labels/continuations under one stable ID, with all pages."""
    merged: dict[str, dict] = {}
    for graphic in graphics:
        existing = merged.get(graphic["id"])
        occurrence = {"pdf_page": graphic["pdf_page"], "printed_page": graphic["printed_page"],
                      "caption_lead": graphic["caption_lead"]}
        if existing is None:
            merged[graphic["id"]] = {**graphic, "caption_occurrences": [occurrence]}
        else:
            existing["caption_occurrences"].append(occurrence)
            if not existing["caption_lead"] and graphic["caption_lead"]:
                existing["caption_lead"] = graphic["caption_lead"]
    return list(merged.values())


def build_index(pdf: Path) -> dict:
    reader = PdfReader(pdf)
    outline = bookmarks(reader)
    tops = [entry for entry in outline if entry["depth"] == 0]
    chapter_heads = [entry for entry in tops if CHAPTER.match(entry["title"])]
    if [int(CHAPTER.fullmatch(e["title"]).group(1)) for e in chapter_heads] != list(range(1, 29)):
        raise ValueError("Expected ordered Chapters 1–28; inspect this PDF edition before indexing.")
    top_by_title = {entry["title"]: entry for entry in tops}
    for title in ("Contents", "Preface", "References", "Index"):
        if title not in top_by_title:
            raise ValueError(f"Missing navigation bookmark: {title}")

    chapters: list[dict] = []
    for head in chapter_heads:
        pos = outline.index(head)
        following = next(entry for entry in tops if entry["pdf_start"] > head["pdf_start"])
        end = following["pdf_start"] - 1
        children = []
        for entry in outline[pos + 1:]:
            if entry["depth"] == 0:
                break
            if entry["title"] == "Chapter Objectives":
                continue
            section = SECTION.match(entry["title"])
            children.append({
                "id": section.group(1) if section else entry["title"].lower().replace(" ", "-"),
                "title": entry["title"], "kind": "section" if section else "navigation",
                "pdf_start": entry["pdf_start"], "location_evidence": "pdf_bookmark",
            })
        number, title = CHAPTER.fullmatch(head["title"]).groups()
        chapters.append({
            "number": int(number), "title": title, "pdf_start": head["pdf_start"], "pdf_end": end,
            "printed_start": head["pdf_start"] - OFFSET, "printed_end": end - OFFSET,
            "entries": add_spans(children, end),
        })

    navigation = []
    for title in ("Contents", "Preface", "References", "Index"):
        entry = top_by_title[title]
        next_pages = [e["pdf_start"] for e in tops if e["pdf_start"] > entry["pdf_start"]]
        end = min(next_pages) - 1 if next_pages else len(reader.pages)
        navigation.append({
            "title": title, "pdf_start": entry["pdf_start"], "pdf_end": end,
            "printed_start": entry["pdf_start"] - OFFSET if title != "Contents" else None,
            "printed_end": end - OFFSET if title != "Contents" else None,
        })

    graphics: list[dict] = []
    footer_checks = []
    contents_text: list[str] = []
    empty_pages = []
    for page_number, page in enumerate(reader.pages, 1):
        text = page.extract_text() or ""
        if not text.strip():
            empty_pages.append(page_number)
        if navigation[0]["pdf_start"] <= page_number <= navigation[0]["pdf_end"]:
            contents_text.append(text)
        printed = printed_footer(text, page_number)
        if printed is not None:
            if page_number - printed != OFFSET:
                raise ValueError(f"Page mapping changed: PDF {page_number}, printed {printed}")
            footer_checks.append(page_number)
        if chapters[0]["pdf_start"] <= page_number < top_by_title["References"]["pdf_start"]:
            graphics.extend(parse_captions(text, page_number))
    if len(footer_checks) < 100:
        raise ValueError("Insufficient printed-footer evidence for the proposed offset.")

    toc = normalized(" ".join(contents_text))
    for chapter in chapters:
        for entry in chapter["entries"]:
            entry["toc_corroborated"] = f"{entry['title']} {entry['printed_start']}" in toc
    graphics = merge_captions(graphics)
    for graphic in graphics:
        if not 1 <= graphic["chapter"] <= 28:
            raise ValueError(f"Unexpected caption chapter: {graphic}")
        chapter = chapters[graphic["chapter"] - 1]
        pages = {o["pdf_page"] for o in graphic["caption_occurrences"]}
        if not all(chapter["pdf_start"] <= page <= chapter["pdf_end"] for page in pages):
            raise ValueError(f"Caption outside its chapter: {graphic}")
        # Page-only association is ambiguous at section boundaries; retain ALL candidates.
        graphic["section_candidates"] = [
            e["id"] for e in chapter["entries"]
            if e["kind"] == "section" and any(e["pdf_start"] <= page <= e["pdf_end"] for page in pages)
        ]

    return {
        "schema_version": 1,
        "source": {
            "id": "anatomy-and-physiology-2e", "filename": FILENAME,
            "title": str(reader.metadata.title), "pdf_pages": len(reader.pages),
            "sha256": hashlib.sha256(pdf.read_bytes()).hexdigest(),
            "pdf_creation_date": str(reader.metadata.get("/CreationDate", "")),
            "citation": "J. Gordon Betts et al., OpenStax, Anatomy and Physiology 2e, © 2026 Rice University (original publication 2022).",
            "source_url": "https://openstax.org/details/books/anatomy-and-physiology-2e",
            "license": "CC BY-NC-SA 4.0; check item-specific exceptions",
            "license_url": "https://creativecommons.org/licenses/by-nc-sa/4.0/",
            "required_attribution": "Access for free at openstax.org.",
            "license_notice_pdf_page": 4, "printed_page_offset": OFFSET,
            "page_mapping_evidence": {
                "method": "every recognized numbered footer checked against PDF page",
                "verified_footer_count": len(footer_checks),
                "first_verified_pdf_page": footer_checks[0], "last_verified_pdf_page": footer_checks[-1],
                "pdf_page_labels_are_printed_numbers": False,
            },
        },
        "coverage": {
            "all_pdf_pages_scanned": True, "empty_text_pdf_pages": empty_pages,
            "graphics_method": "uppercase FIGURE/TABLE caption labels; short first-line locators only; repeated labels retained as caption_occurrences",
            "unlabelled_artwork": "not indexed; discover during rendered-page review",
            "standalone_glossary": "not found in PDF bookmarks or contents; chapter Key Terms indexed instead",
            "appendix": "no appendix entry found in PDF bookmarks or contents",
            "answer_key": "no standalone answer-key entry found in PDF bookmarks or contents",
        },
        "navigation": navigation, "chapters": chapters, "graphics": graphics,
    }


def pdf_link(first: int, last: int | None = None) -> str:
    label = str(first) if last is None or last == first else f"{first}–{last}"
    return f"[{label}]({PDF_LINK}#page={first})"


def cell(text: str) -> str:
    return text.replace("|", "\\|").replace("\n", " ")


def render_chapters(index: dict) -> str:
    source = index["source"]
    lines = [
        "# A&P 2e — Chapter, Subchapter and Navigation Index", "", ATTRIBUTION, "",
        "> Generated by `tools/index_anatomy_physiology.py --write`; do not edit this file manually.",
        "> Add discoveries to `curated-guide.md` and `usage-log.md`; regeneration preserves them.", "",
        f"Local edition fingerprint: `{source['sha256']}`; {source['pdf_pages']} PDF pages.",
        f"Printed + {OFFSET} = PDF, verified against {source['page_mapping_evidence']['verified_footer_count']} numbered footers.",
        "PDF labels are viewer numbers, NOT printed page numbers. Front matter before PDF 17 has no mapped printed number.", "",
        "Chapter spans end before the next chapter. **Entry spans include the next heading's page** because headings may share pages.",
        "These are navigation ranges, not approved extraction ranges. Inspect first/last headings and record partial-page boundaries before reuse.", "",
        "## Book Navigation", "", "| Aid | PDF viewer pages | Printed pages |", "|---|---|---|",
    ]
    for entry in index["navigation"]:
        printed = "unnumbered" if entry["printed_start"] is None else f"{entry['printed_start']}–{entry['printed_end']}"
        lines.append(f"| {entry['title']} | {pdf_link(entry['pdf_start'], entry['pdf_end'])} | {printed} |")
    lines += [
        "", "License/edition notice: " + pdf_link(4) + "; author list: " + pdf_link(3) + ".",
        "No standalone glossary, appendix or answer key was found in the bookmarks/contents. Use each chapter's **Key Terms** as its glossary; use the alphabetical **Index** for cross-chapter terms.",
        "Chapter Objectives/Introduction, Chapter Review, Interactive Link Questions (where present), Review Questions and Critical Thinking Questions are indexed below. Question banks require independent answer checks; optional material cannot become a gate requirement.",
        "References are grouped by source section inside the book's References bookmark; start there for deeper evidence. This index does not copy definitions, question banks or bibliography entries.", "",
        "## Chapter Directory", "", "| Chapter | PDF viewer pages | Printed pages |", "|---|---|---|",
    ]
    for chapter in index["chapters"]:
        lines.append(f"| [{chapter['number']}. {cell(chapter['title'])}](#chapter-{chapter['number']}) | {pdf_link(chapter['pdf_start'], chapter['pdf_end'])} | {chapter['printed_start']}–{chapter['printed_end']} |")
    for chapter in index["chapters"]:
        lines += ["", f"<a id=\"chapter-{chapter['number']}\"></a>", f"## Chapter {chapter['number']}: {chapter['title']}", "",
                  "| Section / study aid | PDF navigation span | Printed span | TOC corroborated |", "|---|---|---|---|"]
        for entry in chapter["entries"]:
            corroborated = "yes" if entry["toc_corroborated"] else "bookmark only; check TOC"
            lines.append(f"| {cell(entry['title'])} | {pdf_link(entry['pdf_start'], entry['pdf_end'])} | {entry['printed_start']}–{entry['printed_end']} | {corroborated} |")
    return "\n".join(lines) + "\n"


def render_graphics(index: dict) -> str:
    lines = [
        "# A&P 2e — Figure and Table Locator Inventory", "", ATTRIBUTION, "",
        "> Generated caption locators, not a cleared asset library. Do not edit; use `curated-guide.md` for teaching notes.",
        "> A caption page may differ from the artwork page. Verify panels, labels, credits and image location visually before extracting.", "",
        "Short caption leads are truncated first lines, **not reliable figure titles**. Section associations are page-based candidates and can overlap at boundaries.",
        "Repeated labels (continued tables) are grouped under one ID with all occurrence pages. Blank leads mean the title was not on the label line; inspect the page.",
        "All entries require rendered-page and item-specific rights review. No images, tables or full captions are reproduced. Unlabelled graphics are outside automated coverage.", "",
        "See [curated useful materials](curated-guide.md) for priorities and suggested teaching uses.",
    ]
    for chapter in index["chapters"]:
        graphics = [g for g in index["graphics"] if g["chapter"] == chapter["number"]]
        lines += ["", f"## Chapter {chapter['number']}: {chapter['title']}", "",
                  "| Stable ID | Short caption lead | PDF caption page | Printed | Section candidates |", "|---|---|---|---|---|"]
        for graphic in graphics:
            occurrences = graphic["caption_occurrences"]
            pages = ", ".join(pdf_link(o["pdf_page"]) for o in occurrences)
            printed = ", ".join(str(o["printed_page"]) for o in occurrences)
            lead = cell(graphic["caption_lead"]) or "[Title not captured; inspect source]"
            lines.append(f"| `{graphic['id']}` | {lead} | {pages} | {printed} | {', '.join(graphic['section_candidates']) or 'Introduction / review'} |")
    return "\n".join(lines) + "\n"


def lookup(index: dict, query: str) -> list[dict]:
    needle = query.casefold()
    records = []
    for chapter in index["chapters"]:
        records.append({"chapter": chapter["number"], "title": chapter["title"], "pdf_start": chapter["pdf_start"], "pdf_end": chapter["pdf_end"]})
        records.extend({"chapter": chapter["number"], **e} for e in chapter["entries"])
    records.extend(index["graphics"])
    records.extend(index["navigation"])
    return [record for record in records if needle in json.dumps(record, ensure_ascii=False).casefold()]


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--pdf", type=Path, default=Path("docs") / FILENAME)
    parser.add_argument("--output-dir", type=Path, default=OUTPUT)
    parser.add_argument("--write", action="store_true", help="Refresh generated index files only.")
    parser.add_argument("--query", help="Search an existing index without opening the PDF.")
    parser.add_argument("--check-source", action="store_true", help="Verify the existing index fingerprint without regenerating.")
    args = parser.parse_args()
    root = args.repo_root.resolve()
    output = root / args.output_dir
    pdf = root / args.pdf
    if args.query is not None or args.check_source:
        index = json.loads((output / "index.json").read_text(encoding="utf-8"))
        if args.check_source:
            if hashlib.sha256(pdf.read_bytes()).hexdigest() != index["source"]["sha256"]:
                raise ValueError("Source PDF changed: regenerate and re-review curated/usage references.")
            print("Source fingerprint matches the index.")
        if args.query is not None:
            print(json.dumps(lookup(index, args.query), ensure_ascii=False, indent=2))
        return 0
    index = build_index(pdf)
    sections = sum(e["kind"] == "section" for c in index["chapters"] for e in c["entries"])
    print(f"Validated {len(index['chapters'])} chapters, {sections} numbered sections, {len(index['graphics'])} figure/table caption locators.")
    print(f"Verified {index['source']['page_mapping_evidence']['verified_footer_count']} printed footers; SHA-256 {index['source']['sha256']}.")
    if args.write:
        output.mkdir(parents=True, exist_ok=True)
        (output / "index.json").write_text(json.dumps(index, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
        (output / "chapters.md").write_text(render_chapters(index), encoding="utf-8")
        (output / "graphics.md").write_text(render_graphics(index), encoding="utf-8")
        print(f"Refreshed generated files in {output}; curated files left untouched.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())