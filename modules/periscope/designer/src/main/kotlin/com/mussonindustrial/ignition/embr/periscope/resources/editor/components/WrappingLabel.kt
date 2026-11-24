package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.client.util.gui.AntialiasLabel
import java.awt.Dimension
import javax.swing.plaf.basic.BasicHTML
import javax.swing.text.View

class WrappingLabel(html: String) : AntialiasLabel("<html>$html") {

    override fun getPreferredSize(): Dimension {
        val preferred = super.getPreferredSize()
        val view = getClientProperty(BasicHTML.propertyKey) as? View
        if (view == null || width <= 0) {
            return preferred
        }

        val insets = insets
        view.setSize((width - insets.left - insets.right).toFloat(), 0f)
        val height = view.getPreferredSpan(View.Y_AXIS).toInt() + insets.top + insets.bottom
        return Dimension(preferred.width, height)
    }

    override fun getMinimumSize(): Dimension {
        return Dimension(0, preferredSize.height)
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val resized = width != this.width
        super.setBounds(x, y, width, height)
        if (resized) {
            revalidate()
        }
    }
}
