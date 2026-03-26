package yaml.visual.preview.plugin.hashizume.online

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.fileEditor.WeighedFileEditorProvider
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.yaml.YAMLFileType

class YamlPreviewFileEditorProvider : WeighedFileEditorProvider(), DumbAware {

    override fun getEditorTypeId(): String = "yaml-visual-preview"

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.fileType is YAMLFileType
    }

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        val textEditor = TextEditorProvider.getInstance()
            .createEditor(project, file) as TextEditor
        val previewPanel = YamlPreviewPanel(project, file)
        return YamlPreviewEditor(
            editor = textEditor,
            preview = previewPanel,
            project = project,
            file = file
        )
    }

    override fun getPolicy(): FileEditorPolicy {
        return FileEditorPolicy.HIDE_OTHER_EDITORS
    }
}
