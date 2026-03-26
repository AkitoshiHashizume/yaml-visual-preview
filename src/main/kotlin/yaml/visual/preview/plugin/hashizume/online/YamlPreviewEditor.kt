package yaml.visual.preview.plugin.hashizume.online

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.TextEditorWithPreview
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Alarm

class YamlPreviewEditor(
    editor: TextEditor,
    private val preview: YamlPreviewPanel,
    private val project: Project,
    private val file: VirtualFile
) : TextEditorWithPreview(
    editor,
    preview,
    "YAML Preview",
    Layout.SHOW_EDITOR_AND_PREVIEW
) {
    private val updateAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
    private val debounceMs = 300

    init {
        // Initial render
        preview.updateHtml(editor.editor.document.text)

        // Live update on document changes
        editor.editor.document.addDocumentListener(object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                scheduleUpdate(event.document.text)
            }
        }, this)
    }

    private fun scheduleUpdate(yamlText: String) {
        updateAlarm.cancelAllRequests()
        updateAlarm.addRequest({ preview.updateHtml(yamlText) }, debounceMs)
    }
}
