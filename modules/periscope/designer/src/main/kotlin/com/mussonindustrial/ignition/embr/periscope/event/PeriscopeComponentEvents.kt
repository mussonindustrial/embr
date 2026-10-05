package com.mussonindustrial.ignition.embr.periscope.event

import com.inductiveautomation.perspective.common.api.ComponentEventDescriptor
import com.inductiveautomation.perspective.designer.workspace.actioneditor.ActionEditorFrame
import com.inductiveautomation.perspective.designer.workspace.actioneditor.EventTreeNodeImpl
import com.jidesoft.tree.TreeModelWrapper
import java.awt.AWTEvent
import java.awt.Component
import java.awt.Container
import java.awt.event.AWTEventListener
import java.awt.event.WindowEvent
import javax.swing.JLabel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreeCellRenderer
import javax.swing.tree.TreePath

object PeriscopeComponentEvents : AWTEventListener {
    const val NAMESPACE = "embr.periscope"

    private val sections =
        listOf(
            Section(
                title = "Lifecycle Events",
                category = "lifecycle",
                events = listOf("onMount", "onUnmount"),
            ),
            Section(
                title = "Visibility Events",
                category = "visibility",
                events = listOf("onVisible", "onHidden"),
            ),
            Section(
                title = "Page Events",
                category = "page",
                events = listOf("onPageVisible", "onPageHidden"),
            ),
        )

    /** `embr.lifecycle.onMount` -> `onMount`, anywhere in a piece of text. */
    private val namespacedName = Regex("""\b$NAMESPACE\.[\w-]+\.(\w+)""")

    private fun shorten(text: String?) = text?.replace(namespacedName, "$1")

    override fun eventDispatched(event: AWTEvent) {
        if (event.id != WindowEvent.WINDOW_OPENED) return
        val frame = event.source as? ActionEditorFrame ?: return
        if (frame.componentPaths == listOf("")) return
        val tree = frame.eventTree
        val wrapper = tree.model as? TreeModelWrapper ?: return
        val model = wrapper.actualModel as? DefaultTreeModel ?: return
        val root = model.root as DefaultMutableTreeNode

        sections.forEachIndexed { index, section ->
            val node = section.asNode()
            model.insertNodeInto(node, root, minOf(index + 1, root.childCount))
            tree.expandPath(TreePath(node.path))
        }

        showShortNames(frame, tree)
    }

    private fun showShortNames(frame: ActionEditorFrame, tree: JTree) {
        tree.cellRenderer = ShortNameRenderer(tree.cellRenderer)

        // The action editor is replaced on each selection; relabel it once it's in place.
        tree.addTreeSelectionListener {
            SwingUtilities.invokeLater { frame.actionCollectionHolder?.let(::shortenLabels) }
        }
    }

    private fun shortenLabels(component: Component) {
        if (component is JLabel) component.text = shorten(component.text)
        if (component is Container) component.components.forEach(::shortenLabels)
    }

    private class ShortNameRenderer(private val renderer: TreeCellRenderer) : TreeCellRenderer {
        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ): Component {
            val component =
                renderer.getTreeCellRendererComponent(
                    tree,
                    value,
                    selected,
                    expanded,
                    leaf,
                    row,
                    hasFocus,
                )
            if (component is JLabel) component.text = shorten(component.text)
            return component
        }
    }

    private data class Section(val title: String, val category: String, val events: List<String>) {
        fun key(event: String) = "${NAMESPACE}.$category.$event"

        fun asNode(): DefaultMutableTreeNode =
            DefaultMutableTreeNode(title).apply {
                events.forEach {
                    add(EventTreeNodeImpl("system", ComponentEventDescriptor(key(it))))
                }
            }
    }
}
