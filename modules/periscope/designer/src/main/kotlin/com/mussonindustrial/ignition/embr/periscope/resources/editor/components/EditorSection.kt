package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel
import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.jidesoft.pane.CollapsiblePane
import java.awt.Dimension
import java.util.prefs.Preferences
import javax.swing.BorderFactory
import javax.swing.JComponent

class EditorSection(
    title: String,
    content: JComponent,
    private val key: String,
    keepExpanded: Boolean = false,
) : CollapsiblePane(title) {

    companion object {
        private val PREFERENCES =
            Preferences.userNodeForPackage(EditorSection::class.java).node("editor-sections")
    }

    init {
        collapsedIcon = VectorIcons.get("carat-right")
        expandedIcon = VectorIcons.get("carat-down")
        style = TREE_STYLE
        border = BorderFactory.createLineBorder(IgnitionLookAndFeel.Colors.Base500)
        isFocusPainted = false
        steps = 0
        contentPane = content
        isContentAreaFilled = true
        isContentBorderVisible = false

        collapse(PREFERENCES.getBoolean(key, false) && !keepExpanded)
        addPropertyChangeListener("collapsed") {
            PREFERENCES.putBoolean(key, isCollapsed)
        }
    }

    override fun getMinimumSize(): Dimension {
        val collapsedMinimum = super.getMinimumSize()
        val fullHeightMinimum = Dimension(collapsedMinimum.width, preferredSize.height)
        return if (isCollapsed) collapsedMinimum else fullHeightMinimum
    }
}
