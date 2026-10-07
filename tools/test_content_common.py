"""Regression tests for the helpers shared by content tools."""

import hashlib
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

from content_common import AP_FILENAME, AP_OFFSET, bounded_pages, metadata, parse_pages, sha256_file, verify_page_count, verify_source
from extract_optional_readings import parse_ranges, parse_reading_block


class CommonTests(unittest.TestCase):
    def test_frontmatter_bom_crlf_and_quoted_or_plain_title(self):
        for title in ['"A: title"', "'A: title'", "A: title"]:
            text = f'\ufeff---\r\nid: lesson-01\r\ntitle: {title}\r\n---\r\n## Read More (Optional)\r\n[Read A&P](reading.md)\r\n'
            fields = metadata(text)
            self.assertEqual(fields["title"], "A: title")
            self.assertEqual(parse_reading_block(Path("lesson.md"), text)[:2], ("lesson-01-optional", "A: title"))

    def test_missing_frontmatter_and_reading_block_fail(self):
        with self.assertRaises(ValueError):
            metadata("No metadata")
        with self.assertRaises(ValueError):
            parse_reading_block(Path("lesson.md"), '---\nid: lesson-01\ntitle: Title\n---\n')

    def test_cli_selection_rejects_empty_tokens_and_huge_ranges(self):
        for value in ["", "1,", "1,,2", "0", "-1", "2-1", "1-999999999"]:
            with self.subTest(value=value), self.assertRaises(ValueError):
                parse_pages(value, 1347)
        self.assertEqual(parse_pages("350,346-350", 1347), [346, 347, 348, 349, 350])

    def test_page_limits_stay_context_specific(self):
        with self.assertRaises(ValueError):
            parse_pages("1-13", 1347)
        self.assertEqual(len(parse_pages("1-20", 1347, 32)), 20)
        self.assertEqual(bounded_pages([3, 1, 3], 10, 32), [1, 3])
        with self.assertRaises(ValueError):
            bounded_pages([0], 10, 32)

    def test_streaming_hash_and_changed_source_guard(self):
        with tempfile.TemporaryDirectory() as directory:
            pdf = Path(directory) / "source.pdf"
            payload = b"a" * (1024 * 1024 + 17)
            pdf.write_bytes(payload)
            digest = hashlib.sha256(payload).hexdigest()
            self.assertEqual(sha256_file(pdf), digest)
            index = {"source": {"sha256": digest, "pdf_pages": 2}}
            self.assertEqual(verify_source(pdf, index), index["source"])
            pdf.write_bytes(b"changed")
            with self.assertRaisesRegex(ValueError, "fingerprint"):
                verify_source(pdf, index)
            with self.assertRaisesRegex(ValueError, "Page count"):
                verify_page_count(3, index["source"])

    def test_pdf_opening_is_cached_for_repeated_ranges(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / AP_FILENAME
            path.touch()
            with patch("extract_optional_readings.PdfReader") as reader:
                reader.return_value.pages = [None] * 400
                spans = parse_ranges("[Read A&P](reading.md). Read PDF pages 340–342 and 350–351.", Path(directory))
                self.assertEqual([(first, last) for _, first, last in spans], [(340, 342), (350, 351)])
                self.assertEqual(reader.call_count, 1)

    def test_ap_source_offset_has_one_shared_value(self):
        from extract_optional_readings import SOURCE_BY_FILENAME
        self.assertEqual(SOURCE_BY_FILENAME[AP_FILENAME.lower()].printed_offset, AP_OFFSET)


if __name__ == "__main__":
    unittest.main()