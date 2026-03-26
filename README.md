# YAML Visual Preview

An IntelliJ IDEA plugin that renders YAML files as styled, readable HTML in a split-pane preview — just like the built-in Markdown preview.

YAML hierarchical structures are automatically mapped to visual elements: headings, tables, and lists.

```
┌──────────────────────────────────────────────────┐
│  [Editor]  [Split]  [Preview]                    │
├───────────────────────┬──────────────────────────┤
│ project:              │ # project                │
│   name: MyApp         │ | Key         | Value  | │
│   version: 2.1.0      │ |-------------|--------| │
│   language: Kotlin    │ | name        | MyApp  | │
│   description:        │ | version     | 2.1.0  | │
│                       │ | language    | Kotlin | │
│ server:               │ | description |(empty) | │
│   host: 0.0.0.0       │                          │
│   port: 8080          │ # server                 │
│   features:           │ | Key  | Value   |       │
│     - cors            │ |------|---------|       │
│     - gzip            │ | host | 0.0.0.0 |       │
│   ssl:                │ | port | 8080    |       │
│     cert_path: ...    │                          │
│     key_path: ...     │ ## features              │
│                       │  • cors                  │
│ environments:         │  • gzip                  │
│   - name: development │                          │
│     ...               │ ## ssl        [− 100% +] │
└───────────────────────┴──────────────────────────┘
```

> Sample YAML: [`market/demo.yml`](market/demo.yml)

## Features

- **Live preview** — Updates automatically as you type (300ms debounce)
- **Structured rendering** — Mappings become headings & tables, lists become bullet points
- **Dark / Light theme** — Follows your IDE theme automatically
- **Zoom controls** — Scale the preview from 50% to 200%
- **Multi-document YAML** — Supports `---` separated documents
- **CJK support** — Proper font rendering for Japanese, Chinese, and Korean text
- **Deep nesting** — Headings from `<h1>` to `<h6>`, with styled levels beyond 6

## Conversion Rules

| YAML Structure | Rendered As |
|---|---|
| Mapping keys | `<h1>` – `<h6>` headings (by depth) |
| All-scalar mappings | Key / Value table |
| Scalar lists | Bulleted list (`<ul>`) |
| Mapping lists | Sections with dashed separators |
| Null / empty values | *(empty)* placeholder |

## Requirements

- **IntelliJ IDEA** 2024.1 – 2025.3.x (Community or Ultimate)
- **JDK 17** or later (for building from source)

## Installation

### From JetBrains Marketplace

Search for **"YAML Visual Preview"** in **Settings → Plugins → Marketplace**.

### From Disk

1. Download or build the plugin ZIP (see [Building](#building))
2. Open **Settings → Plugins → ⚙ → Install Plugin from Disk...**
3. Select the ZIP file and restart the IDE

After installation, open any `.yml` or `.yaml` file — the split preview appears automatically.

## Building

```bash
make build    # Build plugin ZIP
```

The output is at `build/distributions/yaml-visual-preview-<version>.zip`.

### All Make Targets

```bash
make help     # Show all available targets
make build    # Build plugin ZIP
make test     # Run unit tests
make run      # Launch a sandbox IDE for manual testing
make sign     # Build signed plugin (requires signing keys)
make release  # Signed build + show artifact path
make clean    # Clean build artifacts
```

## Project Structure

```
src/main/kotlin/yaml/visual/preview/plugin/hashizume/online/
├── YamlPreviewFileEditorProvider.kt   # FileEditorProvider (entry point)
├── YamlPreviewEditor.kt              # TextEditorWithPreview + live updates
├── YamlPreviewPanel.kt               # JCEF browser rendering
└── YamlToHtmlConverter.kt            # YAML → HTML conversion engine

src/main/resources/META-INF/
├── plugin.xml                        # Plugin descriptor
├── pluginIcon.svg                    # Plugin icon (light theme)
└── pluginIcon_dark.svg               # Plugin icon (dark theme)

src/test/kotlin/.../
└── YamlToHtmlConverterTest.kt        # Unit tests (18 cases)
```

## Tech Stack

- **Kotlin** 2.0 + **Gradle** 8.10
- **IntelliJ Platform SDK** (Gradle Plugin v2.2.1)
- **JCEF** (Chromium bundled with IntelliJ) for HTML rendering
- **SnakeYAML** (bundled with IntelliJ) for YAML parsing

## Contributing

Contributions are welcome! Please open an issue or submit a pull request.

## License

This project is licensed under the [Apache License 2.0](LICENSE).
