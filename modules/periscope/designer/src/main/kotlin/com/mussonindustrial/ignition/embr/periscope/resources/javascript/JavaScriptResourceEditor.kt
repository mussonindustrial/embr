package com.mussonindustrial.ignition.embr.periscope.resources.javascript

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceBuilder
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.gui.tools.DisplayTrackingSyntaxTextArea
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.resources.JavaScriptModule
import java.awt.Font
import java.awt.Label
import java.util.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import net.miginfocom.swing.MigLayout
import org.fife.rsta.ac.LanguageSupportFactory
import org.fife.rsta.ac.js.JavaScriptLanguageSupport
import org.fife.ui.autocomplete.*
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import org.json.JSONException

class JavaScriptResourceEditor(workspace: TabbedResourceWorkspace?, path: ResourcePath?) :
    ResourceEditor<JavaScriptModule>(workspace, path) {

    private var resource: JavaScriptModule? = null
    private lateinit var textArea: DisplayTrackingSyntaxTextArea

    override fun init(resource: JavaScriptModule) {
        this.resource = resource

        this.removeAll()
        this.layout = MigLayout("", "[]", "[25px!][]")

        val title = Label(this.tabTitle)
        title.font = Font(Font.DIALOG_INPUT, Font.PLAIN, 15)
        this.add(title, "cell 0 0, span")

        val lsf = LanguageSupportFactory.get()
        val support =
            lsf.getSupportFor(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT) as JavaScriptLanguageSupport
        support.isStrictMode = true

        textArea = DisplayTrackingSyntaxTextArea(resource.text)
        LanguageSupportFactory.get().register(textArea)
        textArea.apply {
            syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT
            isCodeFoldingEnabled = true
            tabSize = 2
        }

        val sp = RTextScrollPane(textArea)
        this.add(sp, "cell 0 1, span, growx, growy, push")

        textArea.document.addDocumentListener(SimpleDocumentListener { _ -> this.commit() })
    }

    override fun getObjectForSave(): JavaScriptModule {
        return JavaScriptModule(textArea.text ?: "")
    }

    override fun deserialize(resource: ProjectResource): JavaScriptModule {
        return JavaScriptModule(resource.getData(JavaScriptModule.DATA_KEY)?.decodeToString() ?: "")
    }

    @Throws(JSONException::class)
    override fun serializeResource(builder: ProjectResourceBuilder, resource: JavaScriptModule) {
        builder.putData(JavaScriptModule.DATA_KEY, resource.text.encodeToByteArray())
    }
}

class SimpleDocumentListener(val block: (DocumentEvent) -> Unit) : DocumentListener {
    override fun insertUpdate(event: DocumentEvent) {
        block(event)
    }

    override fun removeUpdate(event: DocumentEvent) {
        block(event)
    }

    override fun changedUpdate(event: DocumentEvent) {
        block(event)
    }
}
