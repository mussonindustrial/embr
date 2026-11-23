package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.CompilationStatusIcon.ShapeType
import java.awt.Color
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane
import javax.swing.SwingConstants
import net.miginfocom.swing.MigLayout

class EditorTabs : JTabbedPane() {

    companion object {
        private val BLUE = Color(71, 173, 232)
    }

    enum class Marker(val shape: ShapeType, val color: Color) {
        CONFIGURED(ShapeType.DOT, BLUE),
        ERROR(ShapeType.ERROR, CompilationStatusLabel.RED),
    }

    private val keys = mutableListOf<String>()

    fun addTab(key: String, title: String, content: JComponent) {
        keys += key
        addTab(title, padded(content))

        val label =
            JLabel(title).apply {
                horizontalTextPosition = SwingConstants.LEADING
                iconTextGap = 4
            }
        setTabComponentAt(keys.lastIndex, label)
    }

    fun select(key: String) {
        selectedIndex = keys.indexOf(key).coerceAtLeast(0)
    }

    fun onSelected(listener: (key: String) -> Unit) {
        addChangeListener { listener(keys[selectedIndex]) }
    }

    fun mark(key: String, marker: Marker?) {
        val label = getTabComponentAt(keys.indexOf(key)) as JLabel
        label.icon = marker?.let { CompilationStatusIcon(it.shape, it.color) }
    }

    private fun padded(content: JComponent): JComponent {
        return JPanel(MigLayout("ins 12, fill")).apply { add(content, "push, grow") }
    }
}
