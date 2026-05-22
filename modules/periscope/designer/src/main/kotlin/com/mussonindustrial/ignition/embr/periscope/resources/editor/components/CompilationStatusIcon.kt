package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.RenderingHints
import javax.swing.Icon

class CompilationStatusIcon(
    private val shape: ShapeType,
    private val color: Color,
    private val size: Int = 14,
) : Icon {

    enum class ShapeType {
        DOT,
        WARNING,
        ERROR,
        CHECK,
    }

    override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = color
        g2.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        when (shape) {
            ShapeType.DOT -> g2.fillOval(x + 3, y + 3, size - 6, size - 6)
            ShapeType.WARNING -> {
                val p =
                    Polygon().apply {
                        addPoint(x + size / 2, y + 2)
                        addPoint(x + size - 2, y + size - 2)
                        addPoint(x + 2, y + size - 2)
                    }
                g2.drawPolygon(p)
            }
            ShapeType.ERROR -> {
                val p = 4
                g2.drawLine(x + p, y + p, x + size - p, y + size - p)
                g2.drawLine(x + size - p, y + p, x + p, y + size - p)
            }
            ShapeType.CHECK -> {
                g2.drawLine(x + 4, y + size / 2, x + size / 3 + 1, y + size - 4)
                g2.drawLine(x + size / 3 + 1, y + size - 4, x + size - 3, y + 3)
            }
        }
        g2.dispose()
    }

    override fun getIconWidth(): Int = size

    override fun getIconHeight(): Int = size
}
