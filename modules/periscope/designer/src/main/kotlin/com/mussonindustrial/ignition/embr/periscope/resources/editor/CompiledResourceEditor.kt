package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.jidesoft.swing.JideSplitPane
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.CompiledResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CompileRequest
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.CompileResult
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Compiler
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.DiagnosticSeverity
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.FormatRequest
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.FormatResult
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.CompilationStatusLabel
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.CompileState
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor.Companion.menuItem
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptTextArea
import com.mussonindustrial.ignition.embr.periscope.resources.language.Location
import java.awt.Font
import java.awt.event.ActionEvent
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.Icon
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.KeyStroke
import javax.swing.SwingUtilities.invokeLater
import javax.swing.Timer
import net.miginfocom.swing.MigLayout

abstract class CompiledResourceEditor<T : ClientResource>(
    private val resourceWorkspace: TabbedResourceWorkspace,
    path: ResourcePath,
) : ResourceEditor<T>(resourceWorkspace, path) {

    abstract val compiler: Compiler
    abstract val virtualFileType: String
    // A getter, since init runs before subclasses set their properties.
    abstract val syntaxStyle: String
    abstract val headerIcon: Icon

    lateinit var status: CompilationStatusLabel
    lateinit var editor: ScriptEditor
    lateinit var textArea: ScriptTextArea
    private lateinit var editorSplit: JideSplitPane

    var compileResult: CompileResult? = null
        private set

    var compileState = CompileState.DIRTY
        set(value) {
            field = value
            updateStatus()
        }

    private lateinit var compilationDebounce: Timer
    private var analysis: List<Diagnostic> = emptyList()

    private val virtualFilePath: String
        get() =
            resourcePath.path.toString().let {
                if (it.endsWith(virtualFileType)) it else it + virtualFileType
            }

    protected val compiledContents: CompiledResource.FileContents
        get() {
            val compiled =
                compileResult?.takeIf { it.success }?.output ?: "/* Compilation failed */"
            return CompiledResource.FileContents(textArea.text, compiled)
        }

    protected val compilerMetadata: CompiledResource.CompilerMetadata
        get() = CompiledResource.CompilerMetadata(compiler.version)

    override fun init(resource: T) {
        compilationDebounce = Timer(500) { compile() }.apply { isRepeats = false }
        removeAll()
        layout = MigLayout("ins 8, fill")

        status = CompilationStatusLabel()
        add(
            JLabel(tabTitle, headerIcon, JLabel.LEFT).apply {
                font = Font(Font.MONOSPACED, Font.PLAIN, 14)
                iconTextGap = 6
            },
        )
        add(status, "right, wrap")
        add(createEditorPanel(resource), "push, grow, spanx")

        textArea.document.addDocumentListener(
            SimpleDocumentListener {
                compileState = CompileState.DIRTY
                compilationDebounce.restart()
            },
        )
        bindKey(FORMAT_KEY) { format() }
        invokeLater { compile() }
    }

    private fun createEditorPanel(resource: T): JComponent {
        editor = ScriptEditor(resource.fileContents.source, syntaxStyle)
        textArea = editor.textArea
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

    protected fun bindKey(key: KeyStroke, action: (ActionEvent) -> Unit) {
        editor.bindKey(key, action)
    }

    protected fun addSidePanel(component: JComponent) {
        editorSplit.add(component)
        editorSplit.setProportions(doubleArrayOf(0.85))
    }

    protected fun showAnalysis(diagnostics: List<Diagnostic>) {
        analysis = diagnostics
        showDiagnostics()
    }

    fun format(): FormatResult {
        val result =
            compiler.format(
                FormatRequest(textArea.text, resourcePath.path.toString(), textArea.caretPosition),
            )
        textArea.text = result.output
        textArea.caretPosition = result.cursor
        compile()
        return result
    }

    fun compile(): CompileResult {
        compileState = CompileState.COMPILING
        val result = compiler.compile(CompileRequest(textArea.text, virtualFilePath))
        compileResult = result
        showDiagnostics()

        if (result.success) commit()
        compileState = if (result.success) CompileState.SUCCESS else CompileState.FAILED
        return result
    }

    fun select(start: Int, length: Int) {
        editor.select(start, length)
    }

    open fun reveal(location: Location) {
        editor.reveal(location)
    }

    fun openResource(path: String, then: (CompiledResourceEditor<*>) -> Unit) {
        val target = ResourcePath(ClientResource.type, path)
        resourceWorkspace.open(target) { editor ->
            (editor as? CompiledResourceEditor<*>)?.let { invokeLater { then(it) } }
        }
    }

    private fun showDiagnostics() {
        editor.showDiagnostics(compileResult?.diagnostics.orEmpty() + analysis)
        updateStatus()
    }

    private fun updateStatus() {
        val errors = editor.diagnostics.count { it.severity == DiagnosticSeverity.ERROR }
        status.update(compileState, errors)
    }

    companion object {
        private const val DIVIDER_SIZE = 6
        private val FORMAT_KEY: KeyStroke =
            KeyStroke.getKeyStroke(
                KeyEvent.VK_F,
                InputEvent.SHIFT_DOWN_MASK or InputEvent.ALT_DOWN_MASK,
            )
    }
}
