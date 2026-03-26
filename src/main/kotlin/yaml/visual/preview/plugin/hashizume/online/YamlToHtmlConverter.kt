package yaml.visual.preview.plugin.hashizume.online

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException

class YamlToHtmlConverter {

    fun convert(yamlText: String, darkTheme: Boolean = false): String {
        if (yamlText.isBlank()) {
            return buildFullHtml(emptyList(), darkTheme)
        }
        return try {
            val yaml = Yaml()
            val documents = yaml.loadAll(yamlText).toList()
            buildFullHtml(documents, darkTheme)
        } catch (e: YAMLException) {
            buildErrorHtml(e.message ?: "Invalid YAML", darkTheme)
        } catch (e: Exception) {
            buildErrorHtml("Unexpected error: ${e.message}", darkTheme)
        }
    }

    private fun buildFullHtml(documents: List<Any?>, darkTheme: Boolean): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">")
        sb.append("<style>").append(cssContent(darkTheme)).append("</style>")
        sb.append("</head><body>")
        sb.append("<div id=\"content\">")

        if (documents.isEmpty()) {
            sb.append("<p class=\"empty\">(empty document)</p>")
        } else if (documents.size == 1) {
            renderNode(documents[0], 1, null, sb)
        } else {
            for ((i, doc) in documents.withIndex()) {
                if (i > 0) sb.append("<hr>")
                sb.append(heading(1, "Document ${i + 1}"))
                renderNode(doc, 2, null, sb)
            }
        }

        sb.append("</div>")
        sb.append(zoomControls())
        sb.append("</body></html>")
        return sb.toString()
    }

    private fun buildErrorHtml(message: String, darkTheme: Boolean): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">")
        sb.append("<style>").append(cssContent(darkTheme)).append("</style>")
        sb.append("</head><body>")
        sb.append("<div id=\"content\">")
        sb.append("<div class=\"error-container\">")
        sb.append("<div class=\"error-title\">YAML Parse Error</div>")
        sb.append("<div class=\"error-message\">").append(escapeHtml(message)).append("</div>")
        sb.append("</div>")
        sb.append("</div>")
        sb.append(zoomControls())
        sb.append("</body></html>")
        return sb.toString()
    }

    internal fun renderNode(node: Any?, depth: Int, parentKey: String?, sb: StringBuilder) {
        when {
            node == null -> sb.append("<p class=\"empty\">(empty)</p>")
            node is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                renderMapping(node as Map<String, Any?>, depth, sb)
            }
            node is List<*> -> renderSequence(node, depth, parentKey, sb)
            else -> sb.append("<p>").append(escapeHtml(formatScalar(node))).append("</p>")
        }
    }

    private fun renderMapping(map: Map<String, Any?>, depth: Int, sb: StringBuilder) {
        if (map.isEmpty()) {
            sb.append("<p class=\"empty\">(empty mapping)</p>")
            return
        }

        if (isAllScalar(map)) {
            renderScalarTable(map.entries.map { it.key to it.value }, sb)
            return
        }

        // Mixed: group consecutive scalar entries into tables, complex entries get headings
        val scalarBuffer = mutableListOf<Pair<String, Any?>>()

        for ((key, value) in map) {
            if (isScalar(value)) {
                scalarBuffer.add(key to value)
            } else {
                // Flush scalar buffer as table
                if (scalarBuffer.isNotEmpty()) {
                    renderScalarTable(scalarBuffer.toList(), sb)
                    scalarBuffer.clear()
                }
                // Render complex entry with heading
                sb.append(heading(depth, key))
                renderNode(value, depth + 1, key, sb)
            }
        }

        // Flush remaining scalars
        if (scalarBuffer.isNotEmpty()) {
            renderScalarTable(scalarBuffer.toList(), sb)
        }
    }

    private fun renderScalarTable(entries: List<Pair<String, Any?>>, sb: StringBuilder) {
        sb.append("<table>")
        sb.append("<thead><tr><th>Key</th><th>Value</th></tr></thead>")
        sb.append("<tbody>")
        for ((key, value) in entries) {
            sb.append("<tr>")
            sb.append("<td>").append(escapeHtml(key)).append("</td>")
            sb.append("<td>")
            if (value == null) {
                sb.append("<span class=\"empty\">(empty)</span>")
            } else {
                sb.append(escapeHtml(formatScalar(value)))
            }
            sb.append("</td>")
            sb.append("</tr>")
        }
        sb.append("</tbody></table>")
    }

    private fun renderSequence(items: List<*>, depth: Int, parentKey: String?, sb: StringBuilder) {
        if (items.isEmpty()) {
            sb.append("<p class=\"empty\">(empty list)</p>")
            return
        }

        when {
            isListOfScalars(items) -> renderScalarList(items, sb)
            isListOfMappings(items) -> {
                @Suppress("UNCHECKED_CAST")
                renderMappingList(items as List<Map<String, Any?>>, depth, sb)
            }
            else -> {
                // Mixed list
                sb.append("<ul>")
                for (item in items) {
                    sb.append("<li>")
                    if (isScalar(item)) {
                        sb.append(escapeHtml(formatScalar(item)))
                    } else {
                        renderNode(item, depth, parentKey, sb)
                    }
                    sb.append("</li>")
                }
                sb.append("</ul>")
            }
        }
    }

    private fun renderScalarList(items: List<*>, sb: StringBuilder) {
        sb.append("<ul>")
        for (item in items) {
            sb.append("<li>")
            if (item == null) {
                sb.append("<span class=\"empty\">(empty)</span>")
            } else {
                sb.append(escapeHtml(formatScalar(item)))
            }
            sb.append("</li>")
        }
        sb.append("</ul>")
    }

    private fun renderMappingList(items: List<Map<String, Any?>>, depth: Int, sb: StringBuilder) {
        for ((i, item) in items.withIndex()) {
            if (i > 0) sb.append("<hr class=\"list-separator\">")
            sb.append("<section class=\"list-item\">")
            renderMapping(item, depth, sb)
            sb.append("</section>")
        }
    }

    // --- Zoom controls (embedded in HTML) ---

    private fun zoomControls(): String {
        return """
<div id="zoom-bar">
  <button onclick="zoomOut()" title="Zoom Out">&minus;</button>
  <button onclick="zoomReset()" id="zoom-level" title="Reset Zoom">100%</button>
  <button onclick="zoomIn()" title="Zoom In">+</button>
</div>
<script>
var currentZoom = 100;
function applyZoom() {
  document.getElementById('content').style.zoom = currentZoom + '%';
  document.getElementById('zoom-level').textContent = currentZoom + '%';
}
function zoomIn() {
  if (currentZoom < 200) { currentZoom += 10; applyZoom(); }
}
function zoomOut() {
  if (currentZoom > 50) { currentZoom -= 10; applyZoom(); }
}
function zoomReset() {
  currentZoom = 100; applyZoom();
}
</script>
""".trimIndent()
    }

    // --- Utilities ---

    private fun heading(depth: Int, text: String): String {
        return if (depth in 1..6) {
            "<h$depth>${escapeHtml(text)}</h$depth>"
        } else {
            "<div class=\"heading-deep\" style=\"margin-left:${(depth - 6) * 16}px\">" +
                "<strong>${escapeHtml(text)}</strong></div>"
        }
    }

    private fun escapeHtml(text: String): String = text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    private fun isAllScalar(map: Map<String, Any?>): Boolean =
        map.values.all { isScalar(it) }

    private fun isListOfScalars(list: List<*>): Boolean =
        list.all { isScalar(it) }

    private fun isListOfMappings(list: List<*>): Boolean =
        list.all { it is Map<*, *> }

    private fun isScalar(value: Any?): Boolean =
        value == null || value is String || value is Number || value is Boolean

    private fun formatScalar(value: Any?): String = when (value) {
        null -> "(empty)"
        is Boolean -> if (value) "true" else "false"
        else -> value.toString()
    }

    private fun cssContent(darkTheme: Boolean): String {
        val bg = if (darkTheme) "#2b2b2b" else "#ffffff"
        val fg = if (darkTheme) "#bababa" else "#1a1a1a"
        val borderColor = if (darkTheme) "#555555" else "#dddddd"
        val headingColor = if (darkTheme) "#dcdcdc" else "#333333"
        val tableBg = if (darkTheme) "#313335" else "#fafafa"
        val tableHeaderBg = if (darkTheme) "#3c3f41" else "#f0f0f0"
        val emptyColor = if (darkTheme) "#777777" else "#999999"
        val hrColor = if (darkTheme) "#444444" else "#eeeeee"
        val errorBg = if (darkTheme) "#4e2020" else "#fff0f0"
        val errorBorder = if (darkTheme) "#8b3a3a" else "#ffcccc"
        val errorTitle = if (darkTheme) "#ff6b6b" else "#cc0000"
        val sectionBorder = if (darkTheme) "#4a4a4a" else "#e0e0e0"

        return """
* { margin: 0; padding: 0; box-sizing: border-box; }
body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI",
                 "Hiragino Sans", "Hiragino Kaku Gothic ProN",
                 "Noto Sans JP", "Noto Sans CJK JP",
                 "Yu Gothic", "Meiryo", sans-serif;
    font-size: 14px;
    line-height: 1.7;
    color: $fg;
    background: $bg;
    padding: 20px 24px;
    max-width: 960px;
}
h1, h2, h3, h4, h5, h6 {
    color: $headingColor;
    margin-top: 1.2em;
    margin-bottom: 0.4em;
    font-weight: 600;
    border-bottom: 1px solid $hrColor;
    padding-bottom: 0.2em;
}
h1 { font-size: 1.8em; }
h2 { font-size: 1.5em; }
h3 { font-size: 1.25em; }
h4 { font-size: 1.1em; }
h5 { font-size: 1.0em; }
h6 { font-size: 0.95em; }
h1:first-child, h2:first-child, h3:first-child { margin-top: 0; }
.heading-deep {
    font-size: 0.9em;
    margin-top: 0.8em;
    margin-bottom: 0.3em;
    padding-left: 8px;
    border-left: 3px solid $borderColor;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin: 0.8em 0;
    background: $tableBg;
}
th, td {
    border: 1px solid $borderColor;
    padding: 6px 12px;
    text-align: left;
    vertical-align: top;
}
th {
    background: $tableHeaderBg;
    font-weight: 600;
    font-size: 0.85em;
    text-transform: uppercase;
    letter-spacing: 0.03em;
}
td:first-child {
    font-weight: 500;
    white-space: nowrap;
    width: 1%;
}
ul {
    margin: 0.5em 0 0.5em 1.5em;
}
li {
    margin-bottom: 0.2em;
}
hr {
    border: none;
    border-top: 2px solid $hrColor;
    margin: 1.5em 0;
}
hr.list-separator {
    border-top: 1px dashed $sectionBorder;
    margin: 1em 0;
}
p {
    margin: 0.4em 0;
}
.empty {
    color: $emptyColor;
    font-style: italic;
}
section.list-item {
    padding-left: 8px;
    border-left: 3px solid $sectionBorder;
    margin: 0.5em 0;
    padding-top: 0.2em;
    padding-bottom: 0.2em;
}
.error-container {
    background: $errorBg;
    border: 1px solid $errorBorder;
    border-radius: 6px;
    padding: 16px;
    margin: 16px 0;
}
.error-title {
    color: $errorTitle;
    font-weight: 700;
    font-size: 1.1em;
    margin-bottom: 8px;
}
.error-message {
    font-family: "JetBrains Mono", "Fira Code", monospace;
    font-size: 0.85em;
    white-space: pre-wrap;
    word-break: break-word;
}
#zoom-bar {
    position: fixed;
    bottom: 12px;
    right: 12px;
    display: flex;
    align-items: center;
    gap: 2px;
    background: ${if (darkTheme) "#3c3f41" else "#f0f0f0"};
    border: 1px solid $borderColor;
    border-radius: 6px;
    padding: 2px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
    z-index: 9999;
    opacity: 0.7;
    transition: opacity 0.2s;
}
#zoom-bar:hover {
    opacity: 1.0;
}
#zoom-bar button {
    background: none;
    border: none;
    color: $fg;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    padding: 4px 10px;
    border-radius: 4px;
    line-height: 1;
    min-width: 32px;
}
#zoom-bar button:hover {
    background: ${if (darkTheme) "#505356" else "#dcdcdc"};
}
#zoom-bar #zoom-level {
    font-size: 12px;
    min-width: 48px;
    text-align: center;
}
""".trimIndent()
    }
}
