package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.designer.gui.tools.DisplayTrackingSyntaxTextArea
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.MouseEvent

class ScriptTextArea(text: String) : DisplayTrackingSyntaxTextArea(text) {

    var lineHints: List<LineHint> = emptyList()
        set(value) {
            field = value
            repaint()
        }

    var dimmedLines = 0
        set(value) {
            field = value
            repaint()
        }

    init {
        useFocusableTips = false
    }

    override fun getToolTipTextImpl(e: MouseEvent): String? {
        val supplier = toolTipSupplier ?: return super.getToolTipTextImpl(e)
        return supplier.getToolTipText(this, e)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val graphics = g.create() as Graphics2D
        try {
            paintLineHints(graphics)
            dimLines(graphics)
        } finally {
            graphics.dispose()
        }
    }

    private fun dimLines(g: Graphics2D) {
        if (dimmedLines == 0) return
        val top = modelToView2D(0) ?: return
        val bottom = modelToView2D(getLineEndOffset(dimmedLines - 1) - 1) ?: return
        g.color = Color(background.red, background.green, background.blue, DIM_ALPHA)
        g.fillRect(0, top.y.toInt(), width, (bottom.maxY - top.y).toInt())
    }

    private fun paintLineHints(g: Graphics2D) {
        if (lineHints.isEmpty()) return
        g.font = font.deriveFont(Font.ITALIC)
        g.color = IgnitionLookAndFeel.Colors.DisabledForeground
        val clip = g.clipBounds ?: visibleRect
        lineHints
            .groupBy { getLineOfOffset(it.position.offset) }
            .forEach { (line, hints) ->
                paintLineHint(g, line, hints.joinToString("  ") { it.text }, clip)
            }
    }

    private fun paintLineHint(g: Graphics2D, line: Int, text: String, clip: Rectangle) {
        if (foldManager.isLineHidden(line)) return
        val bounds = modelToView2D(lineTextEnd(line)) ?: return
        if (bounds.maxY < clip.y || bounds.y > clip.maxY) return

        val metrics = g.fontMetrics
        val x = bounds.x + metrics.charWidth(' ') * HINT_GAP
        val y = bounds.y + metrics.ascent
        g.drawString(text, x.toFloat(), y.toFloat())
    }

    private fun lineTextEnd(line: Int): Int {
        val end = getLineEndOffset(line)
        return if (line < lineCount - 1) end - 1 else end
    }

    companion object {
        private const val HINT_GAP = 3
        private const val DIM_ALPHA = 150
    }
}
