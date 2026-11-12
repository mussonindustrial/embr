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
        val foreground = color("ToolTip.foreground")
        val info = signature?.let { content(it, documentation, foreground) } ?: ""
        val style = "body { color: ${hex(foreground)}; width: 300px; }"
        return "<html><style>$style</style><body>$problemsHtml$separator$info</body></html>"
    }

    fun description(signature: String, documentation: String): String {
        val foreground = color("EditorPane.foreground")
        val content = content(signature, documentation, foreground)
        return "<html><body style='color: ${hex(foreground)}'>$content</body></html>"
    }

    private fun content(signature: String, documentation: String, foreground: Color): String {
        return "<h3>${lines(signature)}</h3>${withLinkColor(documentation, foreground)}"
    }

    private fun withLinkColor(html: String, foreground: Color): String {
        val link = hex(blend(foreground, LINK_TINT))
        return html.replace("<a ", "<a style='color: $link' ")
    }

    private fun blend(a: Color, b: Color): Color {
        return Color((a.red + b.red) / 2, (a.green + b.green) / 2, (a.blue + b.blue) / 2)
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

    private fun color(key: String): Color {
        return UIManager.getColor(key) ?: Color.BLACK
    }

    private val LINK_TINT = Color(0x3B, 0x8E, 0xEA)
}
