package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.common.script.typing.ExtensionFunctionDescriptor
import com.inductiveautomation.ignition.common.script.typing.TypeDescriptor
import com.inductiveautomation.ignition.designer.gui.tools.ExtensionFunctionPanel
import com.inductiveautomation.perspective.designer.workspace.scriptseditor.BackendTypeDescriptors
import com.mussonindustrial.ignition.embr.periscope.i18n
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource.Descriptor.DELEGATE_FACTORY
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import java.awt.BorderLayout
import java.awt.Container
import javax.swing.JCheckBox
import javax.swing.JPanel
import net.miginfocom.swing.MigLayout

class ComponentDelegatePanel(delegate: String, enabled: Boolean, onChanged: () -> Unit) :
    JPanel(BorderLayout()) {

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

    private val enabledBox =
        JCheckBox(i18n("component.delegate.enabled"), enabled).apply {
            toolTipText = i18n("component.delegate.enabled.tooltip")
        }

    val isDelegateEnabled: Boolean
        get() = enabledBox.isSelected

    init {
        editor.descriptor = createDescriptor()
        editor.userScript = source.removePrefix(header).ifBlank { EMPTY_BODY }
        editor.document.addDocumentListener(SimpleDocumentListener { onChanged() })
        enabledBox.addActionListener {
            showEnabled()
            onChanged()
        }
        showEnabled()

        add(JPanel(MigLayout("ins 0 0 8 0")).apply { add(enabledBox) }, BorderLayout.NORTH)
        add(editor, BorderLayout.CENTER)
    }

    private fun showEnabled() {
        setEnabledDeep(editor, enabledBox.isSelected)
    }

    private fun setEnabledDeep(component: Container, enabled: Boolean) {
        component.isEnabled = enabled
        component.components.filterIsInstance<Container>().forEach { setEnabledDeep(it, enabled) }
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
