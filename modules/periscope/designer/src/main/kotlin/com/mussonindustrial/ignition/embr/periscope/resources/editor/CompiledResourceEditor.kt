package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceBuilder
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.inductiveautomation.perspective.designer.ui.AbstractEditorFrame
import com.jidesoft.swing.JideSplitPane
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResourceDescriptor
import com.mussonindustrial.ignition.embr.periscope.resources.CompiledResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CompileRequest
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CompileResult
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.DiagnosticSeverity
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.FormatRequest
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.CompilationStatusLabel
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor.Companion.menuItem
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptTextArea
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import com.mussonindustrial.ignition.embr.periscope.resources.pathString
import java.awt.Font
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.util.concurrent.TimeUnit
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.KeyStroke
import javax.swing.SwingUtilities.invokeLater
import javax.swing.Timer
import net.miginfocom.swing.MigLayout

abstract class CompiledResourceEditor<T : ClientResource>(
    private val resourceWorkspace: TabbedResourceWorkspace,
    path: ResourcePath,
) : ResourceEditor<T>(resourceWorkspace, path) {

    abstract val descriptor: ClientResourceDescriptor<T>
    abstract val compiler: Compiler
    abstract val virtualFileType: String
    abstract val syntaxStyle: String
    abstract val headerIcon: Icon

    lateinit var editor: ScriptEditor
        private set

    lateinit var textArea: ScriptTextArea
        private set

    private lateinit var status: CompilationStatusLabel
    private lateinit var titleDetail: JLabel
    private lateinit var editorSplit: JideSplitPane
    private lateinit var compilationDebounce: Timer
    private var compilation: Compilation? = null
    private var analysis: List<Diagnostic> = emptyList()

    val compiled: Boolean?
        get() = compilation?.takeIf { it.source == textArea.text }?.result?.success

    private val virtualFilePath: String
        get() =
            resourcePath.pathString.let {
                if (it.endsWith(virtualFileType)) it else it + virtualFileType
            }

    override fun init(resource: T) {
        if (::compilationDebounce.isInitialized) {
            compilationDebounce.stop()
        }
        compilationDebounce = Timer(500) { compile() }.apply { isRepeats = false }

        removeAll()
        layout = MigLayout("ins 0, fill, gap 0")
        status = CompilationStatusLabel()

        add(createTitlePanel(), "growx, wrap")
        createHeaderPanel(resource)?.let { add(it, "growx, gap 16 16 12 12, wrap") }
        add(createBodyPanel(resource, createEditorPanel(resource)), "push, grow")

        textArea.document.addDocumentListener(SimpleDocumentListener { notifyChanged() })
        editor.bindKey(FORMAT_KEY) { format() }
        invokeLater { compile() }
    }

    private fun createTitlePanel(): JComponent {
        val title =
            JLabel(tabTitle, headerIcon, JLabel.LEFT).apply {
                font = AbstractEditorFrame.TITLE
                iconTextGap = 8
            }

        titleDetail =
            JLabel().apply {
                font = Font(Font.MONOSPACED, Font.PLAIN, font.size)
                foreground = IgnitionLookAndFeel.Colors.DisabledForeground
            }

        return JPanel(MigLayout("ins 0, fillx, gapx 12", "[][grow][]")).apply {
            border =
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, IgnitionLookAndFeel.Colors.Base400),
                    BorderFactory.createEmptyBorder(16, 16, 15, 16),
                )
            add(title)
            add(titleDetail, "aligny baseline")
            add(status)
        }
    }

    protected fun showTitleDetail(detail: String) {
        titleDetail.text = detail
    }

    protected open fun onStatusChanged(hasErrors: Boolean) {}

    protected open fun createHeaderPanel(resource: T): JComponent? {
        return null
    }

    protected open fun createBodyPanel(resource: T, editor: JComponent): JComponent {
        return JPanel(MigLayout(BODY_LAYOUT)).apply { add(editor, "push, grow") }
    }

    protected fun notifyChanged() {
        updateStatus()
        compilationDebounce.restart()
    }

    private fun createEditorPanel(resource: T): JComponent {
        editor = ScriptEditor(resource.fileContents.source, syntaxStyle)
        textArea = editor.textArea
        textArea.parserDelay = 100
        textArea.convertTabsToSpaces()
        textArea.popupMenu?.let { popup ->
            popup.addSeparator()
            popup.add(menuItem("Format", FORMAT_KEY, ::format))
            popup.add(JMenuItem("Compile").apply { addActionListener { compile() } })
        }

        editorSplit =
            JideSplitPane(JideSplitPane.HORIZONTAL_SPLIT).apply {
                add(editor)
                dividerSize = DIVIDER_SIZE
                isProportionalLayout = true
            }
        return editorSplit
    }

    protected fun addSidePanel(component: JComponent) {
        editorSplit.add(component)
        editorSplit.setProportions(doubleArrayOf(0.85))
    }

    protected fun showAnalysis(diagnostics: List<Diagnostic>) {
        analysis = diagnostics
        showDiagnostics()
    }

    fun format() {
        val source = textArea.text
        compiler
            .format(FormatRequest(source, resourcePath.pathString, textArea.caretPosition))
            .thenAccept { result ->
                invokeLater {
                    if (source != textArea.text) return@invokeLater
                    textArea.text = result.output
                    textArea.caretPosition = result.cursor
                }
            }
    }

    fun compile() {
        val source = textArea.text
        compileSource(source).thenAccept { result ->
            invokeLater {
                if (source != textArea.text) return@invokeLater
                record(source, result)
                if (result.success) commit()
            }
        }
    }

    private fun compileSource(source: String) =
        compiler.compile(CompileRequest(source, virtualFilePath))

    private fun record(source: String, result: CompileResult) {
        compilation = Compilation(source, result)
        showDiagnostics()
    }

    override fun getObjectForSave(): T {
        val source = textArea.text
        if (compilation?.source != source) {
            runCatching { compileSource(source).get(SAVE_WAIT_SECONDS, TimeUnit.SECONDS) }
                .onSuccess { record(source, it) }
        }
        val compiled = compilation?.takeIf { it.source == source && it.result.success }?.result
        val output = compiled?.output ?: "/* Compilation failed */"
        return descriptor.create(
            resource.fileLocations,
            CompiledResource.FileContents(source, output),
            CompiledResource.CompilerMetadata(compiler.version),
        )
    }

    override fun deserialize(resource: ProjectResource): T = descriptor.fromResource(resource)

    override fun serializeResource(builder: ProjectResourceBuilder, resource: T) =
        resource.applyToBuilder(builder)

    open fun reveal(location: Location) {
        editor.reveal(location)
    }

    open fun onClosed() {}

    fun openResource(path: String, then: (CompiledResourceEditor<*>) -> Unit) {
        val target = ResourcePath(ClientResource.type, path)
        resourceWorkspace.open(target) { editor ->
            (editor as? CompiledResourceEditor<*>)?.let { invokeLater { then(it) } }
        }
    }

    private fun showDiagnostics() {
        editor.showDiagnostics(compilation?.result?.diagnostics.orEmpty() + analysis)
        updateStatus()
    }

    private fun updateStatus() {
        val errors = editor.diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
        status.update(compiled, errors)
        onStatusChanged(compiled == false || errors > 0)
    }

    private class Compilation(val source: String, val result: CompileResult)

    companion object {
        const val BODY_LAYOUT = "ins 12 16 16 16, fill, wrap 1"
        private const val DIVIDER_SIZE = 6
        private const val SAVE_WAIT_SECONDS = 10L
        private val FORMAT_KEY: KeyStroke =
            KeyStroke.getKeyStroke(
                KeyEvent.VK_F,
                InputEvent.SHIFT_DOWN_MASK or InputEvent.ALT_DOWN_MASK,
            )
    }
}
