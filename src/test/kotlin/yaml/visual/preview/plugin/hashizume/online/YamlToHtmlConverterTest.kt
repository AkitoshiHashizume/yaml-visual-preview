package yaml.visual.preview.plugin.hashizume.online

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class YamlToHtmlConverterTest {

    private lateinit var converter: YamlToHtmlConverter

    @Before
    fun setUp() {
        converter = YamlToHtmlConverter()
    }

    @Test
    fun `empty input returns empty document`() {
        val html = converter.convert("")
        assertContains(html, "(empty document)")
    }

    @Test
    fun `blank input returns empty document`() {
        val html = converter.convert("   \n  \n  ")
        assertContains(html, "(empty document)")
    }

    @Test
    fun `scalar mapping renders as table`() {
        val yaml = """
            name: Alice
            age: 30
            active: true
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "<table>")
        assertContains(html, "name")
        assertContains(html, "Alice")
        assertContains(html, "age")
        assertContains(html, "30")
        assertContains(html, "active")
        assertContains(html, "true")
    }

    @Test
    fun `nested mapping renders headings`() {
        val yaml = """
            config:
              agent: claude
              version: 1
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "<h1>config</h1>")
        assertContains(html, "<table>")
        assertContains(html, "agent")
        assertContains(html, "claude")
    }

    @Test
    fun `scalar list renders as bullet list`() {
        val yaml = """
            commands:
              - pwd
              - ls -la
              - echo hello
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "<h1>commands</h1>")
        assertContains(html, "<ul>")
        assertContains(html, "<li>pwd</li>")
        assertContains(html, "<li>ls -la</li>")
        assertContains(html, "<li>echo hello</li>")
    }

    @Test
    fun `list of mappings renders with separators`() {
        val yaml = """
            items:
              - name: item1
                value: 100
              - name: item2
                value: 200
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "<h1>items</h1>")
        assertContains(html, "item1")
        assertContains(html, "item2")
        assertContains(html, "list-separator")
        assertContains(html, "list-item")
    }

    @Test
    fun `null value renders as empty`() {
        val yaml = """
            key1: null
            key2:
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "(empty)")
    }

    @Test
    fun `deeply nested structure uses appropriate heading levels`() {
        val yaml = """
            level1:
              level2:
                level3:
                  level4:
                    level5:
                      level6:
                        level7:
                          key: deep_value
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "<h1>level1</h1>")
        assertContains(html, "<h2>level2</h2>")
        assertContains(html, "<h3>level3</h3>")
        assertContains(html, "<h4>level4</h4>")
        assertContains(html, "<h5>level5</h5>")
        assertContains(html, "<h6>level6</h6>")
        assertContains(html, "heading-deep")
        assertContains(html, "level7")
        assertContains(html, "deep_value")
    }

    @Test
    fun `japanese keys and values render correctly`() {
        val yaml = """
            設定:
              領域: authentication
              機能ID: feature-email-registration
              画面ID: SCR-EMAIL-REG-001
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "設定")
        assertContains(html, "領域")
        assertContains(html, "authentication")
        assertContains(html, "機能ID")
        assertContains(html, "画面ID")
    }

    @Test
    fun `invalid yaml shows error message`() {
        val yaml = """
            key: [unclosed
              - broken
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "error-container")
        assertContains(html, "YAML Parse Error")
    }

    @Test
    fun `dark theme uses dark colors`() {
        val yaml = "key: value"
        val html = converter.convert(yaml, darkTheme = true)
        assertContains(html, "#2b2b2b")  // dark background
    }

    @Test
    fun `light theme uses light colors`() {
        val yaml = "key: value"
        val html = converter.convert(yaml, darkTheme = false)
        assertContains(html, "#ffffff")  // light background
    }

    @Test
    fun `multi-document yaml renders with document headers`() {
        val yaml = """
            key1: value1
            ---
            key2: value2
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "Document 1")
        assertContains(html, "Document 2")
        assertContains(html, "value1")
        assertContains(html, "value2")
    }

    @Test
    fun `mixed mapping with scalar and complex values`() {
        val yaml = """
            agent: claude
            version: 1
            commands:
              - pwd
              - ls
            data:
              nested_key: nested_value
        """.trimIndent()
        val html = converter.convert(yaml)
        // Scalars should be in a table
        assertContains(html, "agent")
        assertContains(html, "claude")
        // Complex values should have headings
        assertContains(html, "<h1>commands</h1>")
        assertContains(html, "<h1>data</h1>")
    }

    @Test
    fun `html special characters are escaped`() {
        val yaml = """
            key: "<script>alert('xss')</script>"
        """.trimIndent()
        val html = converter.convert(yaml)
        assertFalse(
            "Should not contain unescaped script tag from user input",
            html.contains("<script>alert")
        )
        assertContains(html, "&lt;script&gt;alert")
    }

    @Test
    fun `workflow yaml structure renders correctly`() {
        val yaml = """
            config:
              agent: claude
              pre_command:
                - pwd
                - source ai-workflow/.env.local
            batch:
              - name: mock0
                status: todo
                data:
                  HearingResult:
                    mock_proposal:
                      answer_value:
                        ファイルPath: mock/proposal_1.md
              - name: mock1
                status: todo
                blocked_by:
                  - mock0
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "<h1>config</h1>")
        assertContains(html, "<h1>batch</h1>")
        assertContains(html, "mock0")
        assertContains(html, "mock1")
        assertContains(html, "ファイルPath")
        assertContains(html, "mock/proposal_1.md")
        assertContains(html, "blocked_by")
    }

    @Test
    fun `empty mapping renders empty marker`() {
        // SnakeYAML parses "config:" with no children as config: null
        val yaml = "config:"
        val html = converter.convert(yaml)
        assertContains(html, "(empty)")
    }

    @Test
    fun `empty list renders empty marker`() {
        val yaml = """
            items: []
        """.trimIndent()
        val html = converter.convert(yaml)
        assertContains(html, "(empty list)")
    }

    private fun assertContains(html: String, expected: String) {
        assertTrue(
            "Expected HTML to contain '$expected', but it was not found.\nHTML: ${html.take(500)}...",
            html.contains(expected)
        )
    }
}
