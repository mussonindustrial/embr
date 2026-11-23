package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.common.script.typing.ExtensionFunctionDescriptor
import com.inductiveautomation.ignition.common.script.typing.TypeDescriptor
import com.inductiveautomation.ignition.designer.gui.tools.ExtensionFunctionPanel
import com.inductiveautomation.perspective.designer.workspace.scriptseditor.BackendTypeDescriptors
import com.mussonindustrial.ignition.embr.periscope.i18n
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource.Descriptor.DELEGATE_FACTORY
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import java.awt.BorderLayout
import javax.swing.JPanel

class ComponentDelegatePanel(delegate: String, onChanged: () -> Unit) : JPanel(BorderLayout()) {

    companion object {
        private const val PARAMETER = "component"
        private const val SIGNATURE = "def $DELEGATE_FACTORY($PARAMETER):\n"
        private val SIGNATURE_LINE =
            Regex("^def $DELEGATE_FACTORY\\($PARAMETER\\):[ \\t]*\\n", RegexOption.MULTILINE)
        private const val EMPTY_BODY = "\t"
        private const val COMPONENT_TYPE = "/descriptors/delegate-component.json"
    }

    private val editor = ExtensionFunctionPanel(ExtensionFunctionPanel.GATEWAY_HINTS)
    private val source = delegate.replace("\r\n", "\n")
    private val header = findHeader(source)

    val delegate: String
        get() {
            val body = editor.userScript
            return if (body.isBlank()) "" else header + body
        }

    init {
        editor.descriptor = createDescriptor()
        editor.userScript = source.removePrefix(header).ifBlank { EMPTY_BODY }
        editor.document.addDocumentListener(SimpleDocumentListener { onChanged() })

        add(editor, BorderLayout.CENTER)
    }

    /** Everything up to the function body, or nothing for a script written some other way. */
    private fun findHeader(source: String): String {
        if (source.isBlank()) {
            return SIGNATURE
        }

        val signature = SIGNATURE_LINE.find(source) ?: return ""
        return source.substring(0, signature.range.last + 1)
    }

    private fun createDescriptor(): ExtensionFunctionDescriptor {
        return ExtensionFunctionDescriptor.Builder(DELEGATE_FACTORY)
            .description(i18n("component.delegate.desc"))
            .param(PARAMETER, i18n("component.delegate.param.component"), createComponentType())
            .build()
    }

    private fun createComponentType(): TypeDescriptor {
        return javaClass.getResourceAsStream(COMPONENT_TYPE).use {
            TypeDescriptor.ofJson(it, BackendTypeDescriptors.COMPONENT)
        }
    }
}
