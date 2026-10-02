package com.mussonindustrial.ignition.embr.periscope.action

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.inductiveautomation.ignition.client.util.gui.AntialiasLabel
import com.inductiveautomation.ignition.client.util.gui.CatchAllListener
import com.inductiveautomation.ignition.client.util.gui.HeaderLabel
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.typing.TypeDescriptor
import com.inductiveautomation.ignition.designer.gui.tools.DisplayTrackingSyntaxTextArea
import com.inductiveautomation.perspective.common.config.ActionConfig
import com.inductiveautomation.perspective.designer.api.ActionConfigPanel
import com.inductiveautomation.perspective.designer.api.ActionDesignDelegate
import com.inductiveautomation.perspective.designer.api.ActionEditContext
import com.jidesoft.pane.CollapsiblePane
import com.mussonindustrial.ignition.embr.periscope.Meta
import java.awt.Color
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rtextarea.RTextScrollPane
import org.jdesktop.swingx.JXHyperlink

class JavaScriptActionDesignDelegate :
    ActionDesignDelegate(
        "embr.periscope.javascript",
        ActionConfig.ActionScope.C,
        "${Meta.BUNDLE_PREFIX}.action.javascript.name",
        "${Meta.BUNDLE_PREFIX}.action.javascript.desc",
    ) {

    override fun createEditingPane(context: ActionEditContext): ActionConfigPanel {
        return JavaScriptActionEditor(context)
    }

    private class JavaScriptActionEditor(context: ActionEditContext) :
        ActionConfigPanel(context, MigLayout("ins 0, fill")) {

        companion object {
            const val DESIGNER_HEADER = "async function runAction(self, event) {\n"
            const val CLIENT_HEADER = "async (self, event) => {\n"
            const val FOOTER = "\n}"

            fun body(function: String, header: String) =
                function.removePrefix(header).removeSuffix(FOOTER)
        }

        private val textArea =
            DisplayTrackingSyntaxTextArea("").apply {
                syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT
                antiAliasingEnabled = true
                isCodeFoldingEnabled = true
                tabSize = 2
            }

        init {
            val lineColor = IgnitionLookAndFeel.Colors.Base500

            val parameters =
                JPanel(MigLayout("ins 2 8 8 8, gapx 6, gapy 8, fill")).apply {
                    border = BorderFactory.createMatteBorder(1, 0, 0, 0, lineColor)
                    addParameter(
                        "self",
                        "Component",
                        BundleUtil.i18n("${Meta.BUNDLE_PREFIX}.action.javascript.param.self"),
                    )
                    context.eventObjectSchema?.let { schema ->
                        val event = TypeDescriptor.ofSchema(schema)
                        addParameter("event", event.name, event.description)
                    }
                }

            val parametersPane =
                CollapsiblePane(BundleUtil.i18n("words.parameters")).apply {
                    collapsedIcon = VectorIcons.get("carat-right")
                    expandedIcon = VectorIcons.get("carat-down")
                    style = 1
                    setBorder(BorderFactory.createLineBorder(lineColor))
                    isFocusPainted = false
                    steps = 0
                    contentPane = parameters
                    minimumSize = Dimension(250, 20)
                    isContentAreaFilled = true
                    isContentBorderVisible = false
                }

            add(parametersPane, "growx, h pref!, wrap 16")
            add(HeaderLabel.forKey("words.script"), "wrap 4")
            add(RTextScrollPane(textArea), "push, grow")
        }

        private fun JPanel.addParameter(name: String, type: String, description: String?) {
            val link =
                JXHyperlink().apply {
                    text = name
                    setFocusPainted(false)
                    unclickedColor = Color(0x7E29A3)
                    clickedColor = Color(0x7E29A3)
                    addActionListener {
                        textArea.replaceSelection(name)
                        textArea.requestFocusInWindow()
                    }
                }

            add(link, "newline, width 20%")
            add(
                AntialiasLabel("<html><code>(${type.substringAfterLast('.')})").apply {
                    toolTipText = description ?: type
                },
                "sgx type, width 25%",
            )
            description?.let {
                add(AntialiasLabel("<html>$it"), "sgx description, growx, gapright 3%")
            }
        }

        override fun activate(config: JsonObject?) {
            val function = config?.get("function")?.asString ?: "$CLIENT_HEADER  $FOOTER"
            textArea.text = DESIGNER_HEADER + body(function, CLIENT_HEADER) + FOOTER
            textArea.caretPosition = DESIGNER_HEADER.length
            textArea.discardAllEdits()

            (textArea.document as AbstractDocument).documentFilter = SignatureFilter()
            textArea.document.addDocumentListener(
                CatchAllListener.createCoalesced(context::notifyConfigChanged)
            )
        }

        override fun getConfig(): JsonObject {
            val function = CLIENT_HEADER + body(textArea.text, DESIGNER_HEADER) + FOOTER
            return JsonObject().apply { addProperty("function", function) }
        }

        override fun isScrollable(): Boolean = false

        private class SignatureFilter : DocumentFilter() {
            override fun insertString(
                fb: FilterBypass,
                offset: Int,
                text: String?,
                attrs: AttributeSet?,
            ) = replace(fb, offset, 0, text, attrs)

            override fun remove(fb: FilterBypass, offset: Int, length: Int) =
                replace(fb, offset, length, "", null)

            override fun replace(
                fb: FilterBypass,
                offset: Int,
                length: Int,
                text: String?,
                attrs: AttributeSet?,
            ) {
                val start = DESIGNER_HEADER.length
                val end = fb.document.length - FOOTER.length
                val allowed =
                    if (length == 0) offset in start..end
                    else offset < end && offset + length > start

                if (allowed) {
                    val clampedStart = maxOf(offset, start)
                    val clampedEnd = minOf(offset + length, end)
                    super.replace(fb, clampedStart, clampedEnd - clampedStart, text, attrs)
                }
            }
        }
    }
}
