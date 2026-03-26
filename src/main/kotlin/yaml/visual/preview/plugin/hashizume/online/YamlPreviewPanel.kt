package yaml.visual.preview.plugin.hashizume.online

import com.intellij.ide.ui.LafManager
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.intellij.ui.jcef.JBCefBrowser
import java.beans.PropertyChangeListener
import javax.swing.JComponent
import javax.swing.JLabel

class YamlPreviewPanel(
    private val project: Project,
    private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {

    private val browser: JBCefBrowser? =
        if (JBCefApp.isSupported()) JBCefBrowser() else null

    private val fallbackLabel = JLabel(
        "<html><body style='padding:20px;font-family:sans-serif;'>" +
            "<p>JCEF is not available.</p>" +
            "<p>YAML Visual Preview requires IntelliJ with bundled JCEF support.</p>" +
            "</body></html>"
    )

    private val converter = YamlToHtmlConverter()

    override fun getComponent(): JComponent {
        return browser?.component ?: fallbackLabel
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return browser?.component
    }

    fun updateHtml(yamlText: String) {
        val html = converter.convert(yamlText, isDarkTheme())
        browser?.loadHTML(html)
    }

    private fun isDarkTheme(): Boolean {
        return LafManager.getInstance().currentUIThemeLookAndFeel?.isDark == true
    }

    override fun getName(): String = "YAML Preview"
    override fun isValid(): Boolean = true
    override fun isModified(): Boolean = false
    override fun addPropertyChangeListener(listener: PropertyChangeListener) {}
    override fun removePropertyChangeListener(listener: PropertyChangeListener) {}
    override fun setState(state: FileEditorState) {}
    override fun getFile(): VirtualFile = file

    override fun dispose() {
        browser?.dispose()
    }
}
