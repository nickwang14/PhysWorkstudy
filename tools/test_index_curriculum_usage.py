"""Validate reverse source-use mappings without extracting textbook material."""

import json
import re
import tempfile
import unittest
from pathlib import Path

from index_curriculum_usage import CURRICULUM, SOURCE_DIR, ap_ranges, build_usage, chapter_overlap, metadata, render_usage

ROOT = Path(__file__).resolve().parents[1]
SOURCE = {"filename": "anatomy-and-physiology-2e_-_WEB.pdf", "pdf_pages": 1347,
          "printed_page_offset": 16, "sha256": "test-fingerprint"}


def source_block(first=213, last=224, name="A&P", filename=SOURCE["filename"]):
    return (f"## {name}: PDF pages {first}\u2013{last} "
            f"(printed pages {first - 16}\u2013{last - 16})\n\n"
            f"**Source:** Source PDF: `{filename}`, PDF pages {first}\u2013{last}.\n")


class UsageParserTests(unittest.TestCase):
    def test_frontmatter_accepts_bom_and_scalar_quoting(self):
        result = metadata('\ufeff---\nid: "lesson-01"\ntitle: \'Title: example\'\nstatus: draft\n---\n')
        self.assertEqual(result, {"id": "lesson-01", "title": "Title: example", "status": "draft"})

    def test_other_book_ranges_are_not_assigned_to_ap(self):
        text = source_block() + source_block(602, 607, "Biomechanics", "biomechanics.pdf")
        self.assertEqual(ap_ranges(text, SOURCE), [(213, 224)])

    def test_grouped_ap_blocks_are_retained(self):
        self.assertEqual(ap_ranges(source_block() + source_block(340, 342), SOURCE), [(213, 224), (340, 342)])

    def test_invalid_source_page_and_printed_ranges_are_rejected(self):
        cases = [source_block(1348, 1350), source_block(224, 213),
                 source_block().replace("197\u2013208", "198\u2013209"),
                 source_block(filename="wrong.pdf")]
        for text in cases:
            with self.subTest(text=text), self.assertRaises(ValueError):
                ap_ranges(text, SOURCE)

    def test_one_valid_block_cannot_hide_a_malformed_ap_block(self):
        with self.assertRaises(ValueError):
            ap_ranges(source_block() + "## A&P: PDF pages unknown\n", SOURCE)

    def test_chapter_overlap_clips_and_deduplicates(self):
        chapter = {"pdf_start": 213, "pdf_end": 250}
        self.assertEqual(chapter_overlap([(210, 220), (245, 260), (210, 220)], chapter), [(213, 220), (245, 250)])

    def test_linked_excerpt_lesson_id_is_checked(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            lesson = root / CURRICULUM / "program/chapter-2-anatomy/subchapter-1-bones/lesson-01-test.md"
            lesson.parent.mkdir(parents=True)
            reading = lesson.parent / "reading.md"
            lesson.write_text('---\nid: "lesson-01"\ntitle: "Example"\nprogram: "program"\n'
                              'chapter: "anatomy"\nsubchapter: "bones"\n---\n'
                              '## Read More (Optional)\n[Read A&P](reading.md)\n', encoding="utf-8")
            reading.write_text('---\nid: "lesson-01-optional"\nlesson_id: "wrong"\n---\n' + source_block(), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "lesson ID mismatch"):
                build_usage(root, {"source": SOURCE, "chapters": []})


@unittest.skipUnless((ROOT / SOURCE_DIR / "index.json").is_file(), "Textbook index missing")
class CurrentUsageTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.index = json.loads((ROOT / SOURCE_DIR / "index.json").read_text(encoding="utf-8-sig"))
        cls.usage = build_usage(ROOT, cls.index)

    def test_map_is_current_and_all_chapter_anchors_exist(self):
        text = (ROOT / SOURCE_DIR / "curriculum-usage.md").read_text(encoding="utf-8-sig")
        self.assertEqual(text, render_usage(self.usage, ROOT))
        for chapter in self.index["chapters"]:
            self.assertIn(f'id="textbook-chapter-{chapter["number"]}"', text)

    def test_active_lesson_is_mapped_to_bones_and_joints(self):
        chapters = [c["number"] for c in self.usage["chapters"]
                    if any(u["lesson_id"] == "anatomy-02-02" for u in c["uses"])]
        self.assertEqual(chapters, [6, 9])

    def test_multiple_ranges_and_secondary_ap_source_are_preserved(self):
        by_id = {a["lesson_id"]: a for a in self.usage["assignments"]}
        self.assertEqual(by_id["pattern-02"]["ranges"], [(430, 438), (463, 470)])
        self.assertEqual(by_id["biomechanics-03-14"]["ranges"], [(425, 431)])

    def test_each_usage_is_a_real_linked_reading_and_chapter_overlap(self):
        by_number = {c["number"]: c for c in self.index["chapters"]}
        for chapter in self.usage["chapters"]:
            for use in chapter["uses"]:
                lesson_text = (ROOT / use["lesson_path"]).read_text(encoding="utf-8-sig")
                self.assertEqual(metadata(lesson_text)["id"], use["lesson_id"])
                self.assertEqual(use["chapter_ranges"], chapter_overlap(use["ranges"], by_number[chapter["number"]]))
                self.assertTrue((ROOT / use["reading_path"]).is_file())


if __name__ == "__main__":
    unittest.main()