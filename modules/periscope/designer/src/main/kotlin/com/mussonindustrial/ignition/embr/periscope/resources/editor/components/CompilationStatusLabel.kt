package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel.Colors.DisabledForeground
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.CompilationStatusIcon.ShapeType
import java.awt.Color
import javax.swing.JLabel

class CompilationStatusLabel : JLabel() {

    init {
        iconTextGap = 6
    }

    fun update(state: CompileState, errors: Int) {
        val count = if (errors == 1) "1 Error" else "$errors Errors"
        when {
            state == CompileState.FAILED ->
                show(
                    "$count. Compilation failed. Errors must be resolved before saving.",
                    ShapeType.ERROR,
                    RED,
                )
            state != CompileState.SUCCESS -> show("Compiling...", ShapeType.DOT, DisabledForeground)
            errors > 0 -> show(count, ShapeType.ERROR, RED)
            else -> show("Ready", ShapeType.CHECK, GREEN)
        }
    }

    private fun show(text: String, shape: ShapeType, color: Color) {
        this.text = text
        icon = CompilationStatusIcon(shape, color, 12)
    }

    companion object {
        private val RED = Color(194, 40, 57)
        private val GREEN = Color(46, 139, 87)
    }
}
