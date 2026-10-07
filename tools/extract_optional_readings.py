"""Generate local, attributed optional-reading excerpts from ignored source PDFs.

Run from the repository root after placing the assigned source PDFs in docs/:
    python tools/extract_optional_readings.py
For a focused addition, select only its lesson IDs:
    python tools/extract_optional_readings.py --lesson-id planning-05-01

The output is machine-extracted draft text and must be checked against the PDF.
This script deliberately extracts text only; it does not package PDF pages/images.
"""

from __future__ import annotations

import argparse
import re
from dataclasses import dataclass
from pathlib import Path

from pypdf import PdfReader

from content_common import AP_FILENAME, AP_OFFSET, READ_MORE, REPO_ROOT, metadata


@dataclass(frozen=True)
class Source:
    filename: str
    short_name: str
    citation: str
    license_name: str
    license_url: str
    printed_offset: int
    additional_attribution: str = ""


SOURCES = (
    Source(
        filename=AP_FILENAME,
        short_name="A&P",
        citation="OpenStax, *Anatomy and Physiology 2e*, © 2026 Rice University.",
        license_name="CC BY-NC-SA 4.0",
        license_url="https://creativecommons.org/licenses/by-nc-sa/4.0/",
        printed_offset=AP_OFFSET,
        additional_attribution="Required source notice: “Access for free at openstax.org.”",
    ),
    Source(
        filename="Biomechanics-of-Human-Movement-1600891203._print.pdf",
        short_name="Biomechanics",
        citation="Karine Hamm / OpenStax, *Biomechanics of Human Movement*, © 2016 OpenStax.",
        license_name="CC BY 4.0, except where otherwise noted",
        license_url="https://creativecommons.org/licenses/by/4.0/",
        printed_offset=12,
        additional_attribution=(
            "The supplied source PDF requires this attribution when redistributing an excerpt: "
            "“Download for free at https://cnx.org/contents/Ax2o07Ul@9.73:HR_VN3f7@3/"
            "Introduction-to-Science-and-th” (URL transcribed as printed in that PDF)."
        ),
    ),
    Source(
        filename="Body-Physics-Motion-to-Metabolism-1571156906.pdf",
        short_name="Body Physics",
        citation="Lawrence Davis, *Body Physics: Motion to Metabolism*.",
        license_name="CC BY-NC-SA 4.0, except where otherwise noted",
        license_url="https://creativecommons.org/licenses/by-nc-sa/4.0/",
        printed_offset=26,
    ),
    Source(
        filename="Foundations-of-Exercise-Science-1748368639.pdf",
        short_name="Exercise Science",
        citation=(
            "Laura Ellingson-Sayen and Jennifer Taylor Winney, "
            "*Foundations of Exercise Science*, © 2025."
        ),
        license_name="CC BY-NC 4.0, except where otherwise noted",
        license_url="https://creativecommons.org/licenses/by-nc/4.0/",
        printed_offset=6,
    ),
)

SOURCE_BY_FILENAME = {source.filename.lower(): source for source in SOURCES}
BOOK_MENTION = re.compile(
    r"A&P|Anatomy and Physiology(?: 2e)?|Biomechanics(?: of Human Movement)?|"
    r"Body Physics|Exercise Science",
    re.IGNORECASE,
)
PAGE_RANGE = re.compile(
    r"pages?\s+(\d+)\s*[\u2013\u2014-]\s*(\d+)"
    r"((?:\s*(?:,|and)\s*\d+\s*[\u2013\u2014-]\s*\d+)*)",
    re.IGNORECASE,
)
FRONTMATTER_ID = re.compile(r"^id:\s*[\"']?([^\"'\n]+)", re.MULTILINE)
FRONTMATTER_TITLE = re.compile(r"^title:\s*[\"'](.*?)[\"']\s*$", re.MULTILINE)


def source_from_mention(mention: str) -> Source:
    normalized = mention.lower()
    if normalized.startswith("a&p") or normalized.startswith("anatomy"):
        return SOURCE_BY_FILENAME["anatomy-and-physiology-2e_-_web.pdf"]
    if normalized.startswith("biomechanics"):
        return SOURCE_BY_FILENAME["biomechanics-of-human-movement-1600891203._print.pdf"]
    if normalized.startswith("body physics"):
        return SOURCE_BY_FILENAME["body-physics-motion-to-metabolism-1571156906.pdf"]
    if normalized.startswith("exercise science"):
        return SOURCE_BY_FILENAME["foundations-of-exercise-science-1748368639.pdf"]
    raise ValueError(f"Unrecognized source mention: {mention}")


def parse_reading_block(lesson_path: Path, text: str) -> tuple[str, str, str]:
    fields = metadata(text)
    reading_match = READ_MORE.search(text)
    if not fields.get("id") or not fields.get("title") or not reading_match:
        raise ValueError(f"Missing lesson id, title, or optional reading block: {lesson_path}")

    reading_id = f"{fields['id'].strip()}-optional"
    title = fields["title"].strip()
    block = reading_match.group(1).replace("\n", " ")
    return reading_id, title, block


def parse_ranges(block: str, pdf_dir: Path) -> list[tuple[Source, int, int]]:
    link_match = re.search(r"\[([^\]]+)\]\(([^)]+)\)", block)
    if not link_match:
        raise ValueError("Optional reading must link to its local excerpt")

    primary_mention = BOOK_MENTION.search(link_match.group(1))
    if not primary_mention:
        raise ValueError("Optional reading link label must name its primary source book")
    current_source = source_from_mention(primary_mention.group())

    ranges: list[tuple[Source, int, int]] = []
    # Ignore the linked title when selecting the first range. Link labels may
    # name multiple books, while the link target identifies the primary source.
    previous_end = link_match.end()
    for match in PAGE_RANGE.finditer(block):
        between_ranges = block[previous_end : match.start()]
        mentions = list(BOOK_MENTION.finditer(between_ranges))
        if mentions:
            current_source = source_from_mention(mentions[-1].group())

        grouped_ranges = [(match.group(1), match.group(2))]
        grouped_ranges.extend(
            re.findall(
                r"(\d+)\s*[\u2013\u2014-]\s*(\d+)",
                match.group(3),
            )
        )
        for first_text, last_text in grouped_ranges:
            first, last = int(first_text), int(last_text)
            if first < 1 or last < first:
                raise ValueError(f"Invalid page range {first}-{last}")
            ranges.append((current_source, first, last))
        previous_end = match.end()

    if not ranges:
        raise ValueError("Optional reading contains no inclusive PDF page ranges")

    page_counts: dict[str, int] = {}
    for source, first, last in ranges:
        pdf_path = pdf_dir / source.filename
        if not pdf_path.is_file():
            raise FileNotFoundError(f"Source PDF is missing: {pdf_path}")
        if source.filename not in page_counts:
            page_counts[source.filename] = len(PdfReader(pdf_path).pages)
        page_count = page_counts[source.filename]
        if last > page_count:
            raise ValueError(
                f"Page range {first}-{last} exceeds {source.filename} ({page_count} pages)"
            )
    return ranges


def reading_minutes(registry_path: Path, lesson_id: str) -> int | None:
    if not registry_path.is_file():
        return None
    row = re.search(
        rf"^\|\s*{re.escape(lesson_id)}\s*\|[^|]*\|\s*"
        rf"{re.escape(lesson_id)}-optional\s*\|\s*(\d+)\s*\|\s*$",
        registry_path.read_text(encoding="utf-8"),
        re.MULTILINE,
    )
    return int(row.group(1)) if row else None


def clean_page_text(text: str) -> str:
    lines = [re.sub(r"[ \t]+", " ", line).strip() for line in text.replace("\x00", "").splitlines()]
    cleaned: list[str] = []
    for line in lines:
        if not line and (not cleaned or not cleaned[-1]):
            continue
        if cleaned and cleaned[-1].endswith("-") and line[:1].islower():
            cleaned[-1] = cleaned[-1][:-1] + line
        else:
            cleaned.append(line)
    return "\n".join(cleaned).strip()


def render_excerpt(
    reading_id: str,
    title: str,
    lesson_id: str,
    minutes: int | None,
    ranges: list[tuple[Source, int, int]],
    readers: dict[str, PdfReader],
) -> str:
    duration = f"{minutes}\n" if minutes is not None else "null\n"
    source_word_count = 0
    chunks = [
        "---",
        f'id: "{reading_id}"',
        f'lesson_id: "{lesson_id}"',
        'content_type: "optional_extended_reading"',
        "required_for_gate: false",
        "include_in_texting_curriculum: false",
        'status: "draft"',
        'extraction_status: "machine_extracted_review_required"',
        "source_word_count: 0",
        f"estimated_reading_minutes: {duration.rstrip()}",
        "---",
        f"# {title} — Optional Reading Excerpt",
        "",
        "> **Extraction status: draft.** This text was extracted from the cited local PDF pages. "
        "Line order, spacing, tables, or equations may be imperfect; compare it with the rendered "
        "source before relying on it or publishing it.",
        "",
        "> Optional material: this excerpt is separate from the core lesson, its knowledge check, "
        "and gate requirements.",
        "",
    ]

    for source, first, last in ranges:
        printed_first = first - source.printed_offset
        printed_last = last - source.printed_offset
        chunks.extend(
            [
                f"## {source.short_name}: PDF pages {first}–{last} "
                f"(printed pages {printed_first}–{printed_last})",
                "",
                f"**Source:** {source.citation} Source PDF: `{source.filename}`, "
                f"PDF pages {first}–{last} (inclusive), printed pages {printed_first}–{printed_last}.",
                f"**License:** {source.license_name} ([license terms]({source.license_url})).",
            ]
        )
        if source.additional_attribution:
            chunks.append(f"**Required attribution:** {source.additional_attribution}")
        chunks.append("")

        reader = readers[source.filename]
        for page_number in range(first, last + 1):
            page_text = reader.pages[page_number - 1].extract_text(extraction_mode="layout") or ""
            page_text = clean_page_text(page_text)
            source_word_count += len(re.findall(r"\b[\w’'-]+\b", page_text))
            chunks.extend(
                [
                    f"### PDF page {page_number} (printed page {page_number - source.printed_offset})",
                    "",
                    page_text if page_text else "[No extractable text on this page.]",
                    "",
                ]
            )
    chunks[chunks.index("source_word_count: 0")] = f"source_word_count: {source_word_count}"
    return "\n".join(chunks).rstrip() + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=REPO_ROOT)
    parser.add_argument("--pdf-dir", type=Path, default=Path("docs"))
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("theory-and-knowledge/knowledge/curriculum/foundations-of-movement/optional-readings"),
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate all assignments and report page totals without writing excerpts.",
    )
    parser.add_argument(
        "--lesson-id",
        action="append",
        default=[],
        help="Select one lesson ID to validate/write; may be repeated. Defaults to all authored lessons.",
    )
    args = parser.parse_args()
    repo_root = args.repo_root.resolve()
    pdf_dir = (repo_root / args.pdf_dir).resolve()
    output_dir = (repo_root / args.output_dir).resolve()
    curriculum = repo_root / "theory-and-knowledge/knowledge/curriculum/foundations-of-movement"
    registry_path = curriculum / "reading-options.md"

    readers: dict[str, PdfReader] = {}
    lessons = sorted(
        lesson_path
        for chapter_dir in curriculum.glob("chapter-*-*")
        for lesson_path in chapter_dir.rglob("lesson-*.md")
    )
    if args.lesson_id:
        requested = set(args.lesson_id)
        lessons = [
            lesson_path
            for lesson_path in lessons
            if (match := FRONTMATTER_ID.search(lesson_path.read_text(encoding="utf-8")))
            and match.group(1).strip() in requested
        ]
        found = {
            match.group(1).strip()
            for lesson_path in lessons
            if (match := FRONTMATTER_ID.search(lesson_path.read_text(encoding="utf-8")))
        }
        missing_ids = requested - found
        if missing_ids:
            raise ValueError(f"Lesson IDs not found: {', '.join(sorted(missing_ids))}")

    prepared: list[tuple[Path, str, str, str, int | None, list[tuple[Source, int, int]]]] = []
    page_total = 0
    for lesson_path in lessons:
        text = lesson_path.read_text(encoding="utf-8")
        reading_id, title, block = parse_reading_block(lesson_path, text)
        lesson_id = reading_id.removesuffix("-optional")
        ranges = parse_ranges(block, pdf_dir)
        minutes = reading_minutes(registry_path, lesson_id)
        for source, first, last in ranges:
            page_total += last - first + 1
            if source.filename not in readers:
                readers[source.filename] = PdfReader(pdf_dir / source.filename)
        prepared.append((lesson_path, reading_id, lesson_id, title, minutes, ranges))

    print(f"Validated {len(prepared)} optional readings and {page_total} assigned PDF pages.")
    if args.dry_run:
        for _, reading_id, _, _, _, ranges in prepared:
            summary = "; ".join(f"{source.short_name} {first}–{last}" for source, first, last in ranges)
            print(f"{reading_id}: {summary}")
        return 0

    output_dir.mkdir(parents=True, exist_ok=True)
    for _, reading_id, lesson_id, title, minutes, ranges in prepared:
        destination = output_dir / f"{reading_id}.md"
        destination.write_text(
            render_excerpt(reading_id, title, lesson_id, minutes, ranges, readers),
            encoding="utf-8",
        )
    print(f"Wrote {len(prepared)} draft excerpts to {output_dir}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
