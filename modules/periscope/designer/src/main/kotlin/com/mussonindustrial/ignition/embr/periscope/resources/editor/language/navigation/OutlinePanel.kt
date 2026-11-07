package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.navigation

import com.inductiveautomation.ignition.client.IgnitionLookAndFeel.Colors.DisabledForeground
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor.Companion.menuItem
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.Html
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.LanguageSession
import com.mussonindustrial.ignition.embr.periscope.resources.editor.language.completion.CompletionIcons
import com.mussonindustrial.ignition.embr.periscope.resources.language.OutlineItem
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTree
import javax.swing.Timer
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class OutlinePanel(private val session: LanguageSession, private val editor: ScriptEditor) :
    JPanel(BorderLayout()) {

    private val root = DefaultMutableTreeNode()
    private val model = DefaultTreeModel(root)
    private val tree =
        JTree(model).apply {
            isRootVisible = false
            showsRootHandles = true
            cellRenderer = OutlineRenderer()
        }
    private val collapsed = mutableSetOf<List<String>>()
    private val debounce = Timer(500) { refresh() }.apply { isRepeats = false }
    private val documentListener = SimpleDocumentListener { debounce.restart() }

    init {
        minimumSize = Dimension(120, 0)
        add(JScrollPane(tree))
        tree.componentPopupMenu =
            JPopupMenu().apply {
                add(menuItem("Expand All", null, ::expandAll))
                add(menuItem("Collapse All", null, ::collapseAll))
            }
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    tree.getPathForLocation(e.x, e.y)?.let(::reveal)
                }
            },
        )
        tree.addKeyListener(
            object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    if (e.keyCode == KeyEvent.VK_ENTER) tree.selectionPath?.let(::reveal)
                }
            },
        )
    }

    fun install() {
        editor.textArea.document.addDocumentListener(documentListener)
        refresh()
    }

    fun uninstall() {
        debounce.stop()
        editor.textArea.document.removeDocumentListener(documentListener)
    }

    private fun refresh() {
        session.request({ outline(session.path) }) { show(it.items) }
    }

    private fun show(items: List<OutlineItem>) {
        rememberCollapsed()
        root.removeAllChildren()
        items.forEach { root.add(node(it)) }
        model.reload()
        expandRemembered()
    }

    private fun node(item: OutlineItem): DefaultMutableTreeNode {
        return DefaultMutableTreeNode(item).apply { item.children.forEach { add(node(it)) } }
    }

    private fun expandAll() {
        collapsed.clear()
        expandRemembered()
    }

    private fun collapseAll() {
        for (row in tree.rowCount - 1 downTo 0) tree.collapseRow(row)
        rememberCollapsed()
    }

    private fun rememberCollapsed() {
        collapsed.clear()
        for (row in 0 until tree.rowCount) {
            val path = tree.getPathForRow(row)
            if (!model.isLeaf(path.lastPathComponent) && tree.isCollapsed(path)) {
                collapsed += path.key()
            }
        }
    }

    // rowCount grows as rows expand, so walk until the end.
    private fun expandRemembered() {
        var row = 0
        while (row < tree.rowCount) {
            if (tree.getPathForRow(row).key() !in collapsed) tree.expandRow(row)
            row++
        }
    }

    private fun TreePath.key(): List<String> =
        path.drop(1).map { ((it as DefaultMutableTreeNode).userObject as OutlineItem).text }

    private fun reveal(path: TreePath) {
        val item = (path.lastPathComponent as DefaultMutableTreeNode).userObject as OutlineItem
        editor.select(item.span.start, item.span.length)
    }

    private class OutlineRenderer : DefaultTreeCellRenderer() {
        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ): Component {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus)
            val item = (value as? DefaultMutableTreeNode)?.userObject as? OutlineItem
            if (item != null) {
                val detailColor = Html.hex(if (selected) foreground else DisabledForeground)
                text =
                    "<html><span style='font-family: monospace;'>${Html.escape(item.text)}" +
                        "<font color='$detailColor'>${Html.escape(item.detail)}</font></span>"
                icon = CompletionIcons.of(item.kind, "", selected)
            }
            return this
        }
    }
}
