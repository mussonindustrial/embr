package com.mussonindustrial.ignition.embr.periscope.action

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.inductiveautomation.ignition.client.util.gui.AntialiasLabel
import com.inductiveautomation.ignition.client.util.gui.CatchAllListener
import com.inductiveautomation.ignition.client.util.gui.HeaderLabel
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.script.typing.TypeDescriptor
import com.inductiveautomation.perspective.common.config.ActionConfig
import com.inductiveautomation.perspective.designer.api.ActionConfigPanel
import com.inductiveautomation.perspective.designer.api.ActionDesignDelegate
import com.inductiveautomation.perspective.designer.api.ActionEditContext
import com.jidesoft.pane.CollapsiblePane
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageFeatures
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.ScriptLanguage
import java.awt.Color
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.BorderFactory
import javax.swing.JPanel
import javax.swing.text.AbstractDocument
import javax.swing.text.AttributeSet
import javax.swing.text.DocumentFilter
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
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
            const val DESIGNER_HEADER =
                "/**\n" +
                    " * @param {PerspectiveClient.ComponentStore} self\n" +
                    " * @param {{}} event\n" +
                    " */\n" +
                    "async function runAction(self, event) {\n"
            const val CLIENT_HEADER = "async (self, event) => {\n"
            const val FOOTER = "\n}"

            private val editors = AtomicInteger()

            fun body(function: String, header: String) =
                function.removePrefix(header).removeSuffix(FOOTER)
        }

        private val editor = ScriptEditor(syntaxStyle = SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT)
        private val textArea = editor.textArea
        private val language =
            LanguageFeatures(
                PeriscopeDesignerContext.instance.typeScriptWorkspace,
                editor,
                "__embedded__/actions/javascript-${editors.incrementAndGet()}",
                ScriptLanguage.JAVASCRIPT,
                showDiagnostics = { found ->
                    editor.showDiagnostics(found.filter { !it.isOnSignature() })
                },
            )

        private fun Diagnostic.isOnSignature(): Boolean {
            val line = range?.start?.line ?: return false
            return line <= DESIGNER_HEADER.count { it == '\n' }
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
            add(editor, "push, grow")
        }

        override fun addNotify() {
            super.addNotify()
            language.install()
        }

        override fun removeNotify() {
            language.uninstall()
            super.removeNotify()
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
            textArea.dimmedLines = DESIGNER_HEADER.count { it == '\n' } - 1

            (textArea.document as AbstractDocument).documentFilter = SignatureFilter()
            textArea.document.addDocumentListener(
                CatchAllListener.createCoalesced(context::notifyConfigChanged),
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
