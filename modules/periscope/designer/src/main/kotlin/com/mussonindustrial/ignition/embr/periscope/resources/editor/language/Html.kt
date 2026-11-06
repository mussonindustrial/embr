package com.mussonindustrial.ignition.embr.periscope.resources.editor.language

import java.awt.Color
import javax.swing.UIManager

object Html {
    fun escape(text: String): String {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    }

    fun tooltip(problems: List<String>, signature: String?, documentation: String): String {
        val problemsHtml = problems.joinToString("") { "<p>${escape(it)}</p>" }
        val separator = if (problems.isNotEmpty() && signature != null) "<hr>" else ""
        val info = signature?.let { content(it, documentation) } ?: ""
        val style = "body { color: ${hex("ToolTip.foreground")}; width: 300px; }"
        return "<html><style>$style</style><body>$problemsHtml$separator$info</body></html>"
    }

    fun description(signature: String, documentation: String): String {
        val style = "color: ${hex("EditorPane.foreground")}"
        return "<html><body style='$style'>${content(signature, documentation)}</body></html>"
    }

    private fun content(signature: String, documentation: String): String {
        return "<h3>${lines(signature)}</h3>$documentation"
    }

    private fun lines(text: String): String {
        return escape(text).lines().joinToString("<br>") { line ->
            val indent = line.takeWhile { it == ' ' }.length
            "&#160;".repeat(indent) + line.drop(indent)
        }
    }

    fun hex(color: Color): String {
        return "#%02x%02x%02x".format(color.red, color.green, color.blue)
    }

    private fun hex(colorKey: String): String {
        return hex(UIManager.getColor(colorKey) ?: Color.BLACK)
    }
}
