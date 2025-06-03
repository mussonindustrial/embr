package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.mussonindustrial.ignition.embr.periscope.resources.compiler.Diagnostic
import com.mussonindustrial.ignition.embr.periscope.resources.compiler.DiagnosticSeverity
import java.awt.Color
import org.fife.ui.rtextarea.Gutter

fun Gutter.updateDiagnostics(diagnostics: List<Diagnostic>) {
    removeAllTrackingIcons()
    diagnostics.forEach { diag ->
        val line = (diag.range?.start?.line ?: 1) - 1
        val icon =
            when (diag.severity) {
                DiagnosticSeverity.ERROR ->
                    CompilationStatusIcon(CompilationStatusIcon.ShapeType.ERROR, Color.RED)
                DiagnosticSeverity.WARNING ->
                    CompilationStatusIcon(CompilationStatusIcon.ShapeType.WARNING, Color.ORANGE)
                else -> CompilationStatusIcon(CompilationStatusIcon.ShapeType.DOT, Color.BLUE)
            }
        try {
            addLineTrackingIcon(line, icon, diag.message)
        } catch (_: Exception) {}
    }
}
