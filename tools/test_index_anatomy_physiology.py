"""Unit checks require no PDF; integration checks skip when the source is absent."""

import hashlib
import json
import re
import runpy
import unittest
from pathlib import Path

from pypdf import PdfReader

from index_anatomy_physiology import (
    FILENAME, OFFSET, OUTPUT, add_spans, lookup, merge_captions, parse_captions, pdf_link,
    printed_footer, render_chapters, render_graphics,
)

ROOT = Path(__file__).resolve().parents[1]


class ParserTests(unittest.TestCase):
    def test_frontmatter_is_not_a_footer(self):
        self.assertIsNone(printed_footer("References 1305\nIndex 1309", 16))

    def test_odd_even_and_index_footers(self):
        for text, page, expected in [
            ("1.6 • Anatomical Terminology 23", 39, 23),
            ("24 1 • An Introduction to the Human Body\nAccess for free at openstax.org", 40, 24),
            ("Index     1331", 1347, 1331),
            ("References     1305", 1321, 1305),
        ]:
            self.assertEqual(printed_footer(text, page), expected)

    def test_body_number_is_not_a_footer(self):
        self.assertIsNone(printed_footer("There are 24 vertebrae", 40))

    def test_captions_ignore_cross_references(self):
        result = parse_captions("See Figure 9.10.\nFigure 9.11 describes wear.\nFIGURE 9.10 Types of Synovial Joints\nTABLE 9.1 Joint types", 350)
        self.assertEqual([r["id"] for r in result], ["ap-figure-9.10", "ap-table-9.1"])
        self.assertEqual(result[0]["printed_page"], 334)
        self.assertIn("review_required", result[0]["status"])

    def test_caption_lead_is_bounded(self):
        result = parse_captions("FIGURE 1.1 " + "word " * 100, 23)
        self.assertLessEqual(len(result[0]["caption_lead"]), 111)

    def test_blank_table_label_does_not_consume_footer(self):
        result = parse_captions("TABLE 6.3\n6.3 • Bone Structure 207", 223)
        self.assertEqual(result[0]["caption_lead"], "")

    def test_continued_tables_keep_every_occurrence(self):
        result = merge_captions(parse_captions("TABLE 6.3\n", 223) + parse_captions("TABLE 6.3\n", 224))
        self.assertEqual(len(result), 1)
        self.assertEqual([o["pdf_page"] for o in result[0]["caption_occurrences"]], [223, 224])

    def test_shared_boundary_page_is_preserved(self):
        result = add_spans([{"pdf_start": 39}, {"pdf_start": 45}, {"pdf_start": 45}], 54)
        self.assertEqual([e["pdf_end"] for e in result], [45, 45, 54])
        self.assertTrue(all(e["boundary_review_required"] for e in result))

    def test_unordered_spans_are_rejected(self):
        with self.assertRaises(ValueError):
            add_spans([{"pdf_start": 45}, {"pdf_start": 39}], 54)

    def test_pdf_links_use_one_based_viewer_pages(self):
        self.assertIn("#page=39", pdf_link(39, 45))


@unittest.skipUnless((ROOT / OUTPUT / "index.json").is_file(), "Generate the index first")
class IndexTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.index = json.loads((ROOT / OUTPUT / "index.json").read_text(encoding="utf-8"))

    def test_complete_chapter_navigation(self):
        self.assertEqual([c["number"] for c in self.index["chapters"]], list(range(1, 29)))
        for chapter in self.index["chapters"]:
            titles = {e["title"] for e in chapter["entries"]}
            self.assertTrue({"Key Terms", "Chapter Review", "Review Questions", "Critical Thinking Questions"} <= titles)
            for entry in chapter["entries"]:
                self.assertLessEqual(chapter["pdf_start"], entry["pdf_start"])
                self.assertLessEqual(entry["pdf_start"], entry["pdf_end"])
                self.assertLessEqual(entry["pdf_end"], chapter["pdf_end"])
                self.assertEqual(entry["pdf_start"] - entry["printed_start"], OFFSET)

    def test_graphic_ids_are_unique_and_in_bounds(self):
        graphics = self.index["graphics"]
        self.assertEqual(len(graphics), len({g["id"] for g in graphics}))
        self.assertGreater(len(graphics), 500)
        for g in graphics:
            c = self.index["chapters"][g["chapter"] - 1]
            self.assertTrue(c["pdf_start"] <= g["pdf_page"] <= c["pdf_end"])
            self.assertEqual(g["pdf_page"] - g["printed_page"], OFFSET)

    def test_search_by_term_and_stable_id(self):
        self.assertTrue(lookup(self.index, "synovial"))
        result = lookup(self.index, "ap-figure-9.10")
        self.assertEqual(len(result), 1)
        self.assertEqual(result[0]["pdf_page"], 350)

    def test_generated_markdown_matches_data(self):
        for name, render in [("chapters.md", render_chapters), ("graphics.md", render_graphics)]:
            self.assertEqual((ROOT / OUTPUT / name).read_text(encoding="utf-8"), render(self.index))

    def test_all_entries_corroborate_contents(self):
        self.assertTrue(all(e["toc_corroborated"] for c in self.index["chapters"] for e in c["entries"]))

    def test_curated_graphics_resolve_and_page_links_match(self):
        text = (ROOT / OUTPUT / "curated-guide.md").read_text(encoding="utf-8")
        graphics = {g["id"]: g for g in self.index["graphics"]}
        rows = [line for line in text.splitlines() if line.startswith("| `ap-figure-")]
        self.assertEqual(len(rows), 25)
        for row in rows:
            graphic_id = re.search(r"`(ap-figure-[\d.]+)`", row).group(1)
            page = int(re.search(r"#page=(\d+)", row).group(1))
            self.assertEqual(graphics[graphic_id]["pdf_page"], page)

    def test_local_markdown_links_resolve(self):
        files = list((ROOT / OUTPUT).glob("*.md"))
        files += [ROOT / OUTPUT.parent / "README.md"]
        files += list((ROOT / ".github/skills/textbook-learning-material").rglob("*.md"))
        for path in files:
            for target in re.findall(r"\]\(([^)]+)\)", path.read_text(encoding="utf-8")):
                if target.startswith(("http:", "https:", "#")):
                    continue
                resolved = (path.parent / target.split("#")[0]).resolve()
                if resolved.suffix == ".pdf" and not (ROOT / "docs" / FILENAME).is_file():
                    continue  # Source PDFs are deliberately local-only.
                self.assertTrue(resolved.is_file(), f"Broken link in {path}: {target}")

    @unittest.skipUnless((ROOT / "docs" / FILENAME).is_file(), "Local PDF absent")
    def test_source_fingerprint_and_caption_samples(self):
        pdf = ROOT / "docs" / FILENAME
        self.assertEqual(hashlib.sha256(pdf.read_bytes()).hexdigest(), self.index["source"]["sha256"])
        reader = PdfReader(pdf)
        self.assertEqual(len(reader.pages), self.index["source"]["pdf_pages"])
        # Sample each chapter plus the priority movement figure, without copying captions.
        sample = {next(g["id"] for g in self.index["graphics"] if g["chapter"] == c) for c in range(1, 29)}
        sample.add("ap-figure-9.10")
        for g in self.index["graphics"]:
            if g["id"] in sample:
                text = reader.pages[g["pdf_page"] - 1].extract_text() or ""
                self.assertRegex(text, rf"{g['kind'].upper()}\s+{re.escape(g['number'])}\b")


class ReaderTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.reader_functions = runpy.run_path(str(ROOT / ".github/skills/textbook-learning-material/scripts/read_pdf_pages.py"))

    def test_page_selection_is_inclusive_sorted_and_deduplicated(self):
        parse = self.reader_functions["parse_pages"]
        self.assertEqual(parse("354,346-350,350", 1347), [346, 347, 348, 349, 350, 354])

    def test_invalid_and_overbroad_ranges_are_rejected(self):
        parse = self.reader_functions["parse_pages"]
        for spec in ["0", "-1", "350-346", "1348", "1-13", "1-12,14", "", "1,", "1-999999999"]:
            with self.subTest(spec=spec), self.assertRaises(ValueError):
                parse(spec, 1347)

    def test_skill_frontmatter_and_resources(self):
        skill = ROOT / ".github/skills/textbook-learning-material/SKILL.md"
        text = skill.read_text(encoding="utf-8")
        front = text.split("---", 2)[1]
        name = re.search(r"^name:\s*(.+)$", front, re.MULTILINE).group(1)
        description = re.search(r"^description:\s*'(.+)'$", front, re.MULTILINE).group(1)
        self.assertEqual(name, skill.parent.name)
        self.assertRegex(name, r"^[a-z0-9]+(?:-[a-z0-9]+)*$")
        self.assertTrue(0 < len(description) <= 1024)
        self.assertIn("./scripts/read_pdf_pages.py", text)
        self.assertIn("./assets/learning-brief-template.md", text)


if __name__ == "__main__":
    unittest.main()