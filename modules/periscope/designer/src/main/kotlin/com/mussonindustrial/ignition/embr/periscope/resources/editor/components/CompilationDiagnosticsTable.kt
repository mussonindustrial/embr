package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.jidesoft.grid.SortableTable
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.DiagnosticSeverity
import java.awt.Component
import java.awt.Dimension
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JLabel
import javax.swing.JTable
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import kotlin.collections.forEach

class CompilationDiagnosticsTable : SortableTable() {
    private val diagnosticsModel =
        object : DefaultTableModel(arrayOf("Level", "Line", "Column", "Message"), 0) {
            override fun isCellEditable(row: Int, column: Int): Boolean = false
        }

    var diagnostics: List<Diagnostic> = emptyList()
        private set

    var onNavigate: ((Diagnostic) -> Unit)? = null

    init {
        model = diagnosticsModel
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        rowHeight = 22
        font = Font(Font.DIALOG, Font.PLAIN, 12)
        fillsViewportHeight = true
        showVerticalLines = false
        intercellSpacing = Dimension(0, 0)
        tableHeader.reorderingAllowed = false
        autoCreateRowSorter = true

        defaultCellRenderer =
            object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable,
                    value: Any?,
                    isSelected: Boolean,
                    hasFocus: Boolean,
                    row: Int,
                    column: Int,
                ): Component {
                    val component =
                        super.getTableCellRendererComponent(
                            table,
                            value,
                            isSelected,
                            hasFocus,
                            row,
                            column,
                        ) as JLabel
                    val diagnostic = diagnostics.getOrNull(table.convertRowIndexToModel(row))

                    component.border = BorderFactory.createEmptyBorder(0, 0, 0, 0)

                    if (!isSelected && diagnostic != null) {
                        component.font =
                            if (diagnostic.severity == DiagnosticSeverity.ERROR) {
                                component.font.deriveFont(Font.PLAIN)
                            } else {
                                component.font.deriveFont(Font.PLAIN)
                            }
                    }

                    component.horizontalAlignment = if (column == 0) CENTER else LEFT
                    return component
                }
            }

        columnModel.getColumn(0).apply {
            preferredWidth = 60
            maxWidth = 60
        }
        columnModel.getColumn(1).maxWidth = 60
        columnModel.getColumn(2).maxWidth = 70

        selectionModel.addListSelectionListener {
            val row = selectedRow
            if (row >= 0) {
                val modelRow = convertRowIndexToModel(row)
                diagnostics.getOrNull(modelRow)?.let { onNavigate?.invoke(it) }
            }
        }
    }

    fun updateDiagnostics(newDiagnostics: List<Diagnostic>) {
        this.diagnostics = newDiagnostics
        diagnosticsModel.rowCount = 0
        newDiagnostics.forEach { diagnostic ->
            diagnosticsModel.addRow(
                arrayOf<Any?>(
                    when (diagnostic.severity) {
                        DiagnosticSeverity.ERROR -> "ERROR"
                        DiagnosticSeverity.WARNING -> "WARNING"
                        else -> "DIAGNOSTIC"
                    },
                    diagnostic.range?.start?.line,
                    diagnostic.range?.start?.column,
                    diagnostic.message,
                )
            )
        }
    }
}
