"""Checks for local graphics previews and the human-maintained A&P visual catalogue."""

import json
import re
import tempfile
import unittest
from pathlib import Path

from PIL import Image

from review_textbook_graphics import MAX_PAGES, contact_sheet, review_pages, select_graphics

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "theory-and-knowledge/knowledge/textbook-indices/anatomy-and-physiology-2e"


class PreviewTests(unittest.TestCase):
    def setUp(self):
        self.index = {"graphics": [
            {"id": "ap-figure-9.8", "caption_occurrences": [{"pdf_page": 347}]},
            {"id": "ap-table-6.3", "caption_occurrences": [{"pdf_page": 223}, {"pdf_page": 224}]},
        ]}

    def test_selection_preserves_order_and_deduplicates(self):
        ids = ["ap-table-6.3", "ap-figure-9.8", "ap-table-6.3"]
        self.assertEqual([g["id"] for g in select_graphics(self.index, ids)], ids[:2])

    def test_unknown_and_empty_selections_are_rejected(self):
        for ids in [[], ["ap-figure-999.1"], ["ap-figure-9.8", "wrong"]]:
            with self.subTest(ids=ids), self.assertRaises(ValueError):
                select_graphics(self.index, ids)

    def test_continuation_and_neighbor_pages_are_retained(self):
        selected = select_graphics(self.index, ["ap-table-6.3", "ap-figure-9.8"])
        self.assertEqual(review_pages(selected, [222, 347], 1347), [222, 223, 224, 347])

    def test_out_of_bounds_and_overbroad_batches_are_rejected(self):
        for pages in [[0], [1348], list(range(1, MAX_PAGES + 2))]:
            with self.subTest(pages=pages), self.assertRaises(ValueError):
                review_pages([], pages, 1347)

    def test_contact_sheet_retains_whole_pages_and_header_space(self):
        with tempfile.TemporaryDirectory() as directory:
            paths = []
            for pos, color in enumerate(["red", "blue", "green"]):
                path = Path(directory) / f"{pos}.png"
                image = Image.new("RGB", (120, 160), color)
                image.save(path)
                image.close()
                paths.append(path)
            output = Path(directory) / "contact.png"
            contact_sheet(paths, ["PDF 1", "PDF 2", "PDF 3"], output)
            with Image.open(output) as result:
                self.assertEqual(result.size, (240, 392))
                self.assertEqual(result.getpixel((1, 37)), (255, 0, 0))
                self.assertEqual(result.getpixel((121, 37)), (0, 0, 255))
                self.assertEqual(result.getpixel((1, 233)), (0, 128, 0))


@unittest.skipUnless((SOURCE / "graphic-review.md").is_file(), "Visual catalogue absent")
class CatalogueTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.index = json.loads((SOURCE / "index.json").read_text(encoding="utf-8"))
        cls.text = (SOURCE / "graphic-review.md").read_text(encoding="utf-8")
        cls.blocks = dict(re.findall(
            r"^### (ap-figure-[\d.]+) — [^\n]+\n(.*?)(?=^### |^## |\Z)",
            cls.text, re.MULTILINE | re.DOTALL,
        ))

    def test_reviewed_graphics_resolve_to_correct_caption_pages(self):
        self.assertTrue(self.blocks, "Visual catalogue should contain reviewed entries")
        headings = re.findall(r"^### (ap-figure-[\d.]+) —", self.text, re.MULTILINE)
        self.assertEqual(len(headings), len(set(headings)), "Review IDs must be unique")
        graphics = {g["id"]: g for g in self.index["graphics"]}
        for key, block in self.blocks.items():
            self.assertIn(key, graphics)
            first_link = int(re.search(r"#page=(\d+)", block).group(1))
            self.assertEqual(first_link, graphics[key]["pdf_page"])
            for field in ["**Location:**", "**Visual / priority:**", "**Credit / reuse:**",
                          "**Crop guidance:**", "**Alt-text draft:**", "**Study prompt / lesson fit:**"]:
                self.assertIn(field, block, f"Missing review field for {key}")

    def test_source_version_matches_review(self):
        self.assertIn(self.index["source"]["sha256"], self.text)
        self.assertIn("not domain-expert or legal approval", self.text)

    def test_cross_page_caption_and_third_party_holds_are_recorded(self):
        self.assertIn("#page=355", self.blocks["ap-figure-9.12"])
        self.assertIn("caption continuation", self.blocks["ap-figure-9.12"].lower())
        self.assertIn("Benjamin J. DeLong", self.blocks["ap-figure-6.2"])
        self.assertIn("photograph", self.blocks["ap-figure-6.2"])
        self.assertIn("Regents of University of Michigan Medical School", self.blocks["ap-figure-6.12"])
        self.assertIn("micrograph", self.blocks["ap-figure-6.12"])

    def test_proposed_graphics_have_valid_unique_source_ids(self):
        curated = (SOURCE / "curated-guide.md").read_text(encoding="utf-8")
        proposed = re.findall(r"^\| `(ap-figure-[\d.]+)`", curated, re.MULTILINE)
        source_ids = {graphic["id"] for graphic in self.index["graphics"]}
        self.assertTrue(proposed)
        self.assertEqual(len(proposed), len(set(proposed)))
        self.assertTrue(set(proposed) <= source_ids)

    def test_lesson_matches_reference_real_ids(self):
        for label, target in re.findall(r"^- \[([^\]]+ — [^\]]+)\]\(([^)]+)\)", self.text, re.MULTILINE):
            lesson_id = label.rsplit(" — ", 1)[1]
            lesson = (SOURCE / target).resolve()
            self.assertTrue(lesson.is_file(), target)
            self.assertRegex(lesson.read_text(encoding="utf-8-sig"), rf'^---\s*\nid:\s*"{re.escape(lesson_id)}"')


if __name__ == "__main__":
    unittest.main()