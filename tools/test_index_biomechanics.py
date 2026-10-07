"""Regression tests for the book-specific biomechanics metadata index."""

import hashlib
import json
import unittest
from pathlib import Path

from index_biomechanics import (
    OUTPUT,
    add_spans,
    lookup,
    parse_captions,
    pdf_link,
    printed_footer,
    render_chapters,
    render_graphics,
)

ROOT = Path(__file__).resolve().parents[1]
PDF = ROOT / "theory-and-knowledge/knowledge/textbooks/Biomechanics-of-Human-Movement-1600891203._print.pdf"


class ParserTests(unittest.TestCase):
    def test_reads_both_footer_layouts(self):
        self.assertEqual(printed_footer("8 | 1.1 Understanding Equations and Basic Math"), 8)
        self.assertEqual(printed_footer("1.4 Accuracy and Precision of Measurements | 33"), 33)
        self.assertEqual(printed_footer("708 | 9.7 Interactions of Skeletal Muscles, Their Fascicle Arrangement,"), 708)
        self.assertIsNone(printed_footer("ordinary body text without a page footer"))

    def test_caption_candidates_use_page_based_ids(self):
        text = "See Figure 1 below.\nFigure 1. A movement diagram.\nTable 1. Unit conversions."
        result = parse_captions(text, 93, 81)
        self.assertEqual([item["id"] for item in result], ["bhm-figure-pdf93-1", "bhm-table-pdf93-1"])
        self.assertEqual([item["printed_page"] for item in result], [81, 81])
        self.assertIn("review_required", result[0]["status"])
        other_page = parse_captions("Figure 1. Another diagram.", 95, 83)
        self.assertNotEqual(result[0]["id"], other_page[0]["id"])

    def test_caption_lead_is_bounded(self):
        result = parse_captions("Figure 4. " + "term " * 100, 100, 88)
        self.assertLessEqual(len(result[0]["caption_lead"]), 111)

    def test_shared_boundary_page_is_preserved(self):
        entries = add_spans([{"pdf_start": 15}, {"pdf_start": 21}, {"pdf_start": 21}], 30, 12)
        self.assertEqual([entry["pdf_end"] for entry in entries], [21, 21, 30])
        self.assertEqual(entries[0]["printed_start"], 3)
        self.assertTrue(all(entry["boundary_review_required"] for entry in entries))

    def test_invalid_span_is_rejected(self):
        with self.assertRaises(ValueError):
            add_spans([{"pdf_start": 21}, {"pdf_start": 15}], 30, 12)

    def test_pdf_links_are_one_based(self):
        self.assertEqual(pdf_link(93), "[93](../../textbooks/Biomechanics-of-Human-Movement-1600891203._print.pdf#page=93)")


@unittest.skipUnless((ROOT / OUTPUT / "index.json").is_file(), "Generate the index first")
class IndexTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.index = json.loads((ROOT / OUTPUT / "index.json").read_text(encoding="utf-8"))

    def test_ten_chapters_and_valid_navigation_spans(self):
        self.assertEqual([chapter["number"] for chapter in self.index["chapters"]], list(range(1, 11)))
        self.assertEqual(self.index["chapters"][0]["title"], "Prerequisite Skills for Biomechanics")
        for chapter in self.index["chapters"]:
            self.assertLessEqual(chapter["pdf_start"], chapter["pdf_end"])
            for entry in chapter["entries"]:
                self.assertLessEqual(chapter["pdf_start"], entry["pdf_start"])
                self.assertLessEqual(entry["pdf_start"], entry["pdf_end"])
                self.assertLessEqual(entry["pdf_end"], chapter["pdf_end"])

    def test_page_mapping_and_opening_source_question_are_explicit(self):
        source = self.index["source"]
        self.assertEqual(source["printed_page_offset"], 12)
        self.assertEqual(source["page_mapping_evidence"]["verified_footer_count"], 729)
        self.assertEqual(source["page_mapping_evidence"]["unmapped_pdf_pages_within_content"], [56, 160, 238, 290, 556, 732])
        self.assertIn("different source title", source["source_url_status"])

    def test_caption_candidates_have_unique_in_bounds_ids(self):
        graphics = self.index["graphics"]
        self.assertGreater(len(graphics), 200)
        self.assertEqual(len(graphics), len({item["id"] for item in graphics}))
        chapter_by_number = {chapter["number"]: chapter for chapter in self.index["chapters"]}
        for item in graphics:
            self.assertTrue(chapter_by_number[item["chapter"]]["pdf_start"] <= item["pdf_page"] <= chapter_by_number[item["chapter"]]["pdf_end"])
            self.assertEqual(item["status"], "caption_line_candidate_visual_review_required")

    def test_known_movement_material_locates(self):
        self.assertEqual(len(lookup(self.index, "bhm-figure-pdf93-1")), 1)
        self.assertTrue(lookup(self.index, "synovial"))

    def test_generated_markdown_matches_index(self):
        for name, render in (("chapters.md", render_chapters), ("graphics.md", render_graphics)):
            self.assertEqual((ROOT / OUTPUT / name).read_text(encoding="utf-8"), render(self.index))

    @unittest.skipUnless(PDF.is_file(), "Local PDF absent")
    def test_source_fingerprint_and_page_count(self):
        self.assertEqual(hashlib.sha256(PDF.read_bytes()).hexdigest(), self.index["source"]["sha256"])
        from pypdf import PdfReader

        self.assertEqual(len(PdfReader(PDF).pages), self.index["source"]["pdf_pages"])

    def test_local_markdown_links_resolve(self):
        files = list((ROOT / OUTPUT).glob("*.md")) + [ROOT / OUTPUT.parent / "README.md"]
        import re

        for path in files:
            for target in re.findall(r"\]\(([^)]+)\)", path.read_text(encoding="utf-8")):
                if target.startswith(("http:", "https:", "#")):
                    continue
                resolved = (path.parent / target.split("#")[0]).resolve()
                if resolved.suffix == ".pdf" and not PDF.is_file():
                    continue
                self.assertTrue(resolved.is_file(), f"Broken link in {path}: {target}")


if __name__ == "__main__":
    unittest.main()
