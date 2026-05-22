package com.mussonindustrial.ignition.embr.periscope.resources.editor

import com.inductiveautomation.ignition.common.resourcecollection.ResourcePath
import com.inductiveautomation.ignition.designer.gui.tools.DisplayTrackingSyntaxTextArea
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceEditor
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.jidesoft.swing.*
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.*
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.*
import com.mussonindustrial.ignition.embr.periscope.resources.editor.parser.DiagnosticParser
import java.awt.*
import javax.swing.*
import javax.swing.SwingUtilities.invokeLater
import net.miginfocom.swing.MigLayout
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea
import org.fife.ui.rtextarea.Gutter
import org.fife.ui.rtextarea.RTextScrollPane

abstract class CompiledResourceEditor<T : ClientResource>(
    workspace: TabbedResourceWorkspace,
    path: ResourcePath,
) : ResourceEditor<T>(workspace, path) {

    abstract val compiler: Compiler
    abstract val virtualFileType: String
    open val headerIcon: Icon = PeriscopeIcons.tsx

    lateinit var statusBar: CompilationStatusBar
    lateinit var textArea: RSyntaxTextArea
    lateinit var diagnosticsTable: CompilationDiagnosticsTable
    lateinit var bottomTabs: JideTabbedPane
    lateinit var gutter: Gutter

    var compileResult: CompileResult? = null
    var compileState = CompileState.DIRTY
        set(value) {
            field = value
            updateStatus()
        }

    var dirty = false
        set(value) {
            field = value
            updateStatus()
        }

    private val compilationDebounce: Timer? = Timer(500) { compile() }.apply { isRepeats = false }
    private val parser = DiagnosticParser()

    private val virtualFilePath: String
        get() =
            resourcePath.path.toString().let {
                if (it.endsWith(virtualFileType)) it else it + virtualFileType
            }

    override fun init(resource: T) {
        removeAll()
        layout = BorderLayout()

        val mainSplit =
            JideSplitPane(JideSplitPane.VERTICAL_SPLIT).apply {
                add(createEditorPanel(resource))
                add(createBottomTabs())
                isProportionalLayout = false
                dividerSize = 8
            }

        add(createHeader(), BorderLayout.NORTH)
        add(mainSplit, BorderLayout.CENTER)

        setupListeners()
    }

    private fun createHeader(): JPanel {
        val header =
            JPanel(MigLayout("ins 8 12 8 12, fillx", "[shrink][grow, fill][]", "[]")).apply {
                border = PartialEtchedBorder(PartialEtchedBorder.LOWERED, PartialSide.SOUTH)
            }
        val title =
            StyledLabel(tabTitle, headerIcon, JLabel.LEFT).apply {
                font = Font(Font.DIALOG, Font.BOLD, 16)
                iconTextGap = 8
            }
        header.add(title)

        invokeLater { title.icon = headerIcon }

        return header
    }

    private fun createEditorPanel(resource: T): JPanel {
        textArea =
            DisplayTrackingSyntaxTextArea(resource.fileContents.source).apply {
                antiAliasingEnabled = true
                isCodeFoldingEnabled = true
                tabSize = 2
                popupMenu?.let { popup ->
                    popup.addSeparator()
                    popup.add(JMenuItem("Format").apply { addActionListener { format() } })
                    popup.add(JMenuItem("Compile").apply { addActionListener { compile() } })
                }
            }

        val scrollPane =
            RTextScrollPane(textArea).apply {
                minimumSize = Dimension(0, 300)
                isIconRowHeaderEnabled = true
            }
        gutter = scrollPane.gutter
        statusBar = CompilationStatusBar()

        return JPanel(BorderLayout()).apply {
            add(scrollPane, BorderLayout.CENTER)
            add(statusBar, BorderLayout.SOUTH)
        }
    }

    private fun createBottomTabs(): JideTabbedPane {
        diagnosticsTable =
            CompilationDiagnosticsTable().apply { onNavigate = ::navigateToDiagnostic }
        bottomTabs =
            JideTabbedPane(JideTabbedPane.TOP).apply {
                preferredSize = Dimension(420, 200)
                minimumSize = Dimension(280, 100)
                isBoldActiveTab = true
                addTab("Problems", JideScrollPane(diagnosticsTable))
            }
        return bottomTabs
    }

    private fun setupListeners() {
        textArea.document.addDocumentListener(
            SimpleDocumentListener {
                dirty = true
                compileState = CompileState.DIRTY
                compilationDebounce?.restart()
            }
        )

        invokeLater {
            textArea.addParser(parser)
            compile()
        }
    }

    fun format(): FormatResult {
        val result =
            compiler.format(
                FormatRequest(textArea.text, resourcePath.path.toString(), textArea.caretPosition)
            )
        textArea.text = result.output
        textArea.caretPosition = result.cursor
        dirty = true
        compile()
        return result
    }

    fun compile(): CompileResult {
        compileState = CompileState.COMPILING
        val result = compiler.compile(CompileRequest(textArea.text, virtualFilePath))
        compileResult = result

        applyCompileResult(result)

        if (result.success) {
            dirty = false
            commit()
            compileState = CompileState.SUCCESS
        } else {
            compileState = CompileState.FAILED
        }
        return result
    }

    private fun applyCompileResult(result: CompileResult) {
        updateStatus()
        updateDiagnostics(result)
        parser.setDiagnostics(result.diagnostics)
        textArea.forceReparsing(parser)
    }

    private fun updateStatus() {
        val result = compileResult
        val errors = result?.diagnostics?.count { it.severity == DiagnosticSeverity.ERROR } ?: 0
        val warnings = result?.diagnostics?.count { it.severity == DiagnosticSeverity.WARNING } ?: 0
        val message =
            if (result?.success == false)
                "Compilation failed. Errors must be resolved before saving."
            else null

        statusBar.update(dirty, compileState, errors, warnings, message)
    }

    private fun updateDiagnostics(result: CompileResult) {
        bottomTabs.setTitleAt(0, "Problems (${result.diagnostics.size})")
        diagnosticsTable.updateDiagnostics(result.diagnostics)
        gutter.updateDiagnostics(result.diagnostics)
    }

    private fun navigateToDiagnostic(diagnostic: Diagnostic) {
        try {
            val line = diagnostic.range!!.start.line - 1
            val column = diagnostic.range!!.start.column - 1
            textArea.caretPosition = textArea.getLineStartOffset(line) + column
            textArea.requestFocusInWindow()
        } catch (_: Exception) {}
    }
}
