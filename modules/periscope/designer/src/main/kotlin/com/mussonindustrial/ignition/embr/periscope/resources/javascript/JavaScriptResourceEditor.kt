package com.mussonindustrial.ignition.embr.periscope.resources.javascript

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceBuilder
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.resources.JavaScriptModule
import java.awt.Font
import java.awt.Label
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import org.json.JSONException

class JavaScriptResourceEditor(workspace: TabbedResourceWorkspace?, path: ResourcePath?) :
    ResourceEditor<JavaScriptModule>(workspace, path) {

    private var resource: JavaScriptModule? = null
    private lateinit var textArea: RSyntaxTextArea

    override fun init(resource: JavaScriptModule) {
        this.resource = resource

        this.removeAll()
        this.layout = MigLayout("", "[]", "[25px!][]")

        val title = Label(this.tabTitle)
        title.font = Font(Font.DIALOG_INPUT, Font.PLAIN, 15)
        this.add(title, "cell 0 0, span")

        textArea = RSyntaxTextArea(resource.text)
        textArea.syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
        textArea.isCodeFoldingEnabled = true
        val sp = RTextScrollPane(textArea)
        this.add(sp, "cell 0 1, span, growx, growy, push")

        textArea.document.addDocumentListener(SimpleDocumentListener { _ -> this.commit() })
    }

    override fun getObjectForSave(): JavaScriptModule {
        return JavaScriptModule(textArea.text)
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
