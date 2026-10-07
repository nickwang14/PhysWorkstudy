"""Validate single skill ownership, equivalent capabilities and generated adapters."""

import tempfile
import unittest
from pathlib import Path

from content_common import REPO_ROOT, metadata
from setup_agent_platforms import AGENTS, COPILOT, SKILLS, configure, make_alias, render_copilot_agent


class PlatformTests(unittest.TestCase):
    def test_real_platform_configuration_is_current(self):
        expected = (len(list((REPO_ROOT / SKILLS).glob("*/SKILL.md"))),
                    len(list((REPO_ROOT / AGENTS).glob("*.agent.md"))))
        self.assertEqual(configure(REPO_ROOT, check=True), expected)

    def test_canonical_skill_uses_portable_frontmatter(self):
        skill = REPO_ROOT / ".agents/skills/textbook-learning-material/SKILL.md"
        fields = metadata(skill.read_text(encoding="utf-8-sig"))
        self.assertEqual(fields["name"], skill.parent.name)
        self.assertTrue(set(fields) <= {"name", "description", "license", "compatibility", "metadata", "allowed-tools"})
        self.assertLessEqual(len(fields["description"]), 1024)
        self.assertFalse((REPO_ROOT / ".claude/skills/textbook-parsing/SKILL.md").exists())

    def test_all_adapter_bodies_and_names_match_canonical_profiles(self):
        for agent in (REPO_ROOT / AGENTS).glob("*.agent.md"):
            rendered = render_copilot_agent(agent)
            saved = (REPO_ROOT / COPILOT / agent.name).read_text(encoding="utf-8-sig")
            self.assertEqual(saved, rendered)
            self.assertEqual(metadata(saved)["name"], metadata(agent.read_text(encoding="utf-8-sig"))["name"])

    def test_read_only_and_non_execution_roles_remain_restricted(self):
        for name in ["physio-consultant.agent.md", "legal-compliance.agent.md"]:
            tools = metadata((REPO_ROOT / AGENTS / name).read_text(encoding="utf-8-sig"))["tools"]
            self.assertNotIn("Edit", tools)
            self.assertNotIn("Write", tools)
            self.assertNotIn("Bash", tools)
            aliases = metadata(render_copilot_agent(REPO_ROOT / AGENTS / name))["tools"]
            self.assertNotIn("edit", aliases)
            self.assertNotIn("execute", aliases)
        content = metadata(render_copilot_agent(REPO_ROOT / AGENTS / "content-strategist.agent.md"))["tools"]
        self.assertNotIn("execute", content)

    def test_setup_refuses_to_erase_real_skill_content(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            target = root / "canonical"
            target.mkdir()
            alias = root / "existing"
            alias.mkdir()
            (alias / "SKILL.md").write_text("user content", encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "nonempty"):
                make_alias(alias, target)
            self.assertEqual((alias / "SKILL.md").read_text(encoding="utf-8"), "user content")

    def test_unmapped_native_tools_fail_instead_of_widening_permissions(self):
        with tempfile.TemporaryDirectory() as directory:
            agent = Path(directory) / "agent.md"
            agent.write_text('---\nname: Example\ndescription: Example role\ntools: [UnknownTool]\n---\nBody\n', encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "Unmapped tools"):
                render_copilot_agent(agent)


if __name__ == "__main__":
    unittest.main()