"""Reverse-map linked A&P optional readings to textbook/curriculum chapters.

Uses the saved textbook index and excerpt metadata; does not require local PDFs,
regenerate readings, or infer that recommended graphics are embedded in lessons.
Default validates; --write refreshes only curriculum-usage.md.
"""

from __future__ import annotations

import argparse
import json
import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CURRICULUM = Path("theory-and-knowledge/knowledge/curriculum")
SOURCE_DIR = Path("theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e")
READ_MORE = re.compile(r"^## Read More \(Optional\)\s*(.*?)(?=^## |\Z)", re.MULTILINE | re.DOTALL)
LINK = re.compile(r"\]\(([^)]+)\)")
SOURCE_HEADING = re.compile(
    r"^## ([^\n]+): PDF pages (\d+)[\u2013\u2014-](\d+) "
    r"\(printed pages (\d+)[\u2013\u2014-](\d+)\)\s*$", re.MULTILINE,
)


def metadata(text: str) -> dict[str, str]:
    """Read the flat scalar frontmatter used by lesson/excerpt reference fields."""
    text = text.removeprefix("\ufeff")
    match = re.match(r"\A---\s*\n(.*?)\n---(?:\n|$)", text, re.DOTALL)
    if not match:
        raise ValueError("Missing frontmatter")
    result = {}
    for key, value in re.findall(r"^([\w-]+):[ \t]*(.*?)\s*$", match.group(1), re.MULTILINE):
        if len(value) >= 2 and value[0] == value[-1] and value[0] in "\"'":
            value = value[1:-1]
        result[key] = value
    return result


def ap_ranges(text: str, source: dict) -> list[tuple[int, int]]:
    """Use explicitly attributed excerpt headings; never mix other-book pages."""
    result = []
    for match in SOURCE_HEADING.finditer(text):
        if match.group(1) != "A&P":
            continue
        first, last, printed_first, printed_last = map(int, match.groups()[1:])
        if not 1 <= first <= last <= source["pdf_pages"]:
            raise ValueError(f"Out-of-range A&P assignment: {first}-{last}")
        if (first - printed_first, last - printed_last) != (source["printed_page_offset"],) * 2:
            raise ValueError(f"Incorrect A&P printed-page mapping: {first}-{last}")
        next_heading = re.search(r"^## ", text[match.end():], re.MULTILINE)
        end = match.end() + next_heading.start() if next_heading else len(text)
        block = text[match.end():end]
        if f"Source PDF: `{source['filename']}`" not in block:
            raise ValueError("A&P heading has no matching source-PDF attribution")
        result.append((first, last))
    if len(re.findall(r"^## A&P:", text, re.MULTILINE)) != len(result):
        raise ValueError("Unrecognized A&P source heading; inspect excerpt format")
    return result


def chapter_overlap(ranges: list[tuple[int, int]], chapter: dict) -> list[tuple[int, int]]:
    return sorted({(max(first, chapter["pdf_start"]), min(last, chapter["pdf_end"]))
                   for first, last in ranges
                   if first <= chapter["pdf_end"] and last >= chapter["pdf_start"]})


def build_usage(root: Path, index: dict) -> dict:
    if not (root / CURRICULUM).is_dir():
        raise FileNotFoundError(f"Curriculum directory missing: {root / CURRICULUM}")
    lessons = sorted((root / CURRICULUM).rglob("lesson-*.md"))
    assignments = []
    without_reading = 0
    for lesson in lessons:
        text = lesson.read_text(encoding="utf-8-sig")
        block = READ_MORE.search(text)
        if not block:
            without_reading += 1
            continue
        links = LINK.findall(block.group(1))
        if len(links) != 1:
            raise ValueError(f"Expected one optional-reading link: {lesson}")
        excerpt = (lesson.parent / links[0].split("#")[0]).resolve()
        if not excerpt.is_relative_to(root.resolve()) or excerpt.suffix != ".md" or not excerpt.is_file():
            raise ValueError(f"Missing/unsupported local excerpt: {lesson}: {links[0]}")
        reading_text = excerpt.read_text(encoding="utf-8-sig")
        spans = ap_ranges(reading_text, index["source"])
        if not spans:
            continue
        lesson_meta = metadata(text)
        reading_meta = metadata(reading_text)
        required = {"id", "title", "program", "chapter", "subchapter"}
        if not required <= lesson_meta.keys():
            raise ValueError(f"Missing lesson reference metadata: {lesson}")
        if reading_meta.get("lesson_id") != lesson_meta["id"]:
            raise ValueError(f"Linked excerpt lesson ID mismatch: {lesson}")
        if reading_meta.get("id") != lesson_meta["id"] + "-optional":
            raise ValueError(f"Linked reading ID mismatch: {excerpt}")
        chapter_dir = next((p for p in lesson.parents if re.match(r"chapter-\d+-", p.name)), None)
        if chapter_dir is None:
            raise ValueError(f"No curriculum chapter directory: {lesson}")
        assignments.append({
            "lesson_id": lesson_meta["id"], "title": lesson_meta["title"],
            "program": lesson_meta["program"], "curriculum_chapter": int(chapter_dir.name.split("-")[1]),
            "chapter_slug": lesson_meta["chapter"], "subchapter": lesson_meta["subchapter"],
            "lesson_path": lesson.relative_to(root).as_posix(),
            "reading_path": excerpt.relative_to(root.resolve()).as_posix(),
            "reading_id": reading_meta["id"], "reading_status": reading_meta.get("status", "unspecified"),
            "ranges": spans,
        })
    chapters = []
    for chapter in index["chapters"]:
        uses = []
        for assignment in assignments:
            overlap = chapter_overlap(assignment["ranges"], chapter)
            if overlap:
                uses.append({**assignment, "chapter_ranges": overlap})
        chapters.append({"number": chapter["number"], "title": chapter["title"], "uses": uses})
    return {"lessons_scanned": len(lessons), "without_reading": without_reading,
            "assignments": assignments, "chapters": chapters, "source": index["source"]}


def relative_link(root: Path, path: str) -> str:
    return Path(os.path.relpath(root / path, root / SOURCE_DIR)).as_posix()


def escape(text: str) -> str:
    return text.replace("|", "\\|").replace("\n", " ")


def render_usage(usage: dict, root: Path) -> str:
    lines = [
        "# A&P — Where Textbook Chapters Are Used in Our Curriculum", "",
        "> Generated by `python tools/index_curriculum_usage.py --write`; do not edit manually.",
        "", "## Scope and Meaning", "",
        f"Scanned **{usage['lessons_scanned']} authored lessons** across `theory-and-knowledge/knowledge/curriculum/`; "
        f"**{len(usage['assignments'])} lessons have linked A&P optional-reading assignments**.",
        f"Source edition SHA-256: `{usage['source']['sha256']}`; page mapping comes from [the textbook index](index.json).", "",
        "**Recorded use** means an A&P-attributed page range in the optional reading currently linked by a lesson. "
        "It does not mean the chapter is required for a gate, the excerpt is approved, or every topic on those pages is taught.",
        "Assignments may include other books; only the A&P ranges are shown here. Chapter-crossing ranges appear under each affected textbook chapter, clipped to that chapter. "
        "A lesson can therefore appear more than once; chapter counts must not be summed as distinct lessons.",
        "Curriculum chapters and subchapters come from lesson metadata/placement. Outline-only modules, informal drafting consultations and core-lesson claims without explicit source records are outside this map. "
        "**No recorded assignment** is not proof that a textbook chapter has never been consulted.",
        "Suggested graphics are **not assigned use or embedded assets**; see the separate [visual catalogue and lesson suggestions](graphic-review.md) and [usage history](usage-log.md).", "",
        "## Textbook Chapter → Curriculum Overview", "",
        "| A&P textbook chapter | Linked lessons | Curriculum destinations |",
        "|---|---:|---|",
    ]
    for chapter in usage["chapters"]:
        destinations = sorted({(u["program"], u["curriculum_chapter"], u["chapter_slug"]) for u in chapter["uses"]})
        location = "<br>".join(f"{escape(p)} / Ch {n}: {escape(s.replace('-', ' '))}" for p, n, s in destinations)
        lines.append(f"| [{chapter['number']}. {escape(chapter['title'])}](#textbook-chapter-{chapter['number']}) | {len(chapter['uses'])} | {location or 'No linked A&P optional-reading assignment found'} |")
    for chapter in usage["chapters"]:
        if not chapter["uses"]:
            continue
        lines += ["", f"<a id=\"textbook-chapter-{chapter['number']}\"></a>",
                  f"## A&P Chapter {chapter['number']}: {chapter['title']}", "",
                  "| Curriculum chapter / subchapter | Lesson | A&P PDF / printed pages used | Linked optional reading / status |",
                  "|---|---|---|---|"]
        for use in chapter["uses"]:
            destination = f"{escape(use['program'])} / Ch {use['curriculum_chapter']}: {escape(use['chapter_slug'].replace('-', ' '))}<br>Subchapter: {escape(use['subchapter'].replace('-', ' '))}"
            lesson = f"[{escape(use['title'])}]({relative_link(root, use['lesson_path'])})<br>`{use['lesson_id']}`"
            ranges = "<br>".join(f"PDF {a}–{b} / printed {a - usage['source']['printed_page_offset']}–{b - usage['source']['printed_page_offset']}" for a, b in use["chapter_ranges"])
            reading = f"[{use['reading_id']}]({relative_link(root, use['reading_path'])}) / {escape(use['reading_status'])}"
            lines.append(f"| {destination} | {lesson} | {ranges} | {reading} |")
    # Give unused overview links real targets without adding empty detail tables.
    lines += ["", "## Chapters Without Recorded Assignments", ""]
    for chapter in usage["chapters"]:
        if not chapter["uses"]:
            lines += [f"<a id=\"textbook-chapter-{chapter['number']}\"></a>",
                      f"- **{chapter['number']}. {chapter['title']}** — no linked A&P optional reading found."]
    lines += ["", "## Keep This Map Current", "",
              "Update the lesson's linked reading and its attributed source spans, then rerun `python tools/index_curriculum_usage.py --write`. "
              "The command needs only the committed textbook index and local Markdown readings, not the ignored PDFs. "
              "It changes only this map, never lessons, excerpts, visual-review decisions or usage history.",
              "Run `python tools/index_curriculum_usage.py --check` to detect a stale map without writing; "
              "keep consultation, graphic embedding and review evidence in the source usage log rather than inventing assigned use."]
    return "\n".join(lines) + "\n"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--repo-root", type=Path, default=ROOT)
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--write", action="store_true")
    mode.add_argument("--check", action="store_true", help="Fail if the saved map is absent or stale.")
    args = parser.parse_args()
    root = args.repo_root.resolve()
    index = json.loads((root / SOURCE_DIR / "index.json").read_text(encoding="utf-8-sig"))
    usage = build_usage(root, index)
    rendered = render_usage(usage, root)
    path = root / SOURCE_DIR / "curriculum-usage.md"
    if args.write:
        path.write_text(rendered, encoding="utf-8")
    if args.check and (not path.is_file() or path.read_text(encoding="utf-8-sig") != rendered):
        parser.error("Curriculum usage map is stale; run with --write.")
    print(f"Scanned {usage['lessons_scanned']} lessons; {len(usage['assignments'])} linked A&P assignments across "
          f"{sum(bool(c['uses']) for c in usage['chapters'])} textbook chapters.")
    if args.write:
        print(f"Wrote {path}; lessons/readings and human review records unchanged.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())