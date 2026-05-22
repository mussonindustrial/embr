package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import java.awt.Color
import javax.swing.Icon

class CompilationStatusBar : StatusBar() {
    fun update(
        isDirty: Boolean,
        state: CompileState?,
        errors: Int,
        warnings: Int,
        message: String?,
    ) {
        val blue = Color(0, 122, 204)
        val red = Color(194, 40, 57)

        var backgroundColor = blue
        var newStateText = ""
        var newStateIcon: Icon? = null
        val newMessage = message ?: ""

        if (isDirty || state == CompileState.COMPILING) {
            newStateText = "Compiling..."
            newStateIcon = CompilationStatusIcon(CompilationStatusIcon.ShapeType.DOT, Color.WHITE)
        } else if (state == CompileState.SUCCESS) {
            newStateText = "Ready"
            newStateIcon = CompilationStatusIcon(CompilationStatusIcon.ShapeType.CHECK, Color.WHITE)
        }

        if (state == CompileState.FAILED) {
            backgroundColor = red
            val count = errors + warnings
            newStateText = if (count == 1) "1 Error" else "$count Errors"
            newStateIcon = CompilationStatusIcon(CompilationStatusIcon.ShapeType.ERROR, Color.WHITE)
        }

        background = backgroundColor
        stateLabel.text = newStateText
        stateLabel.icon = newStateIcon

        messageLabel.text = newMessage
        messageLabel.isVisible = newMessage.isNotEmpty()
    }
}
