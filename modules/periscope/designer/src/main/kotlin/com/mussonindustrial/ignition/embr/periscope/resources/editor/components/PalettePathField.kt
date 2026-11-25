package com.mussonindustrial.ignition.embr.periscope.resources.editor.components

import com.jidesoft.combobox.TreeComboBox
import com.jidesoft.converter.ConverterContext
import com.jidesoft.converter.ObjectConverter
import com.mussonindustrial.ignition.embr.periscope.component.addChild
import com.mussonindustrial.ignition.embr.periscope.component.childNodes
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import java.awt.Component
import java.awt.Container
import javax.swing.JTree
import javax.swing.text.JTextComponent
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath

class PalettePathField(
    palettePaths: List<List<String>>,
    initialPath: String,
    onChanged: () -> Unit,
) : TreeComboBox(DefaultTreeModel(createRoot(palettePaths), true)) {

    companion object {
        private fun createRoot(palettePaths: List<List<String>>): DefaultMutableTreeNode {
            val root = DefaultMutableTreeNode()
            palettePaths
                .sortedBy { it.joinToString("/").lowercase() }
                .forEach { folders ->
                    folders.fold(root) { parent, name ->
                        parent.childNodes().find { it.userObject == name } ?: parent.addChild(name)
                    }
                }
            return root
        }

        private fun getPath(value: Any?): String {
            return when (value) {
                is TreePath -> value.path.drop(1).joinToString("/")
                null -> ""
                else -> value.toString()
            }
        }
    }

    private object PathConverter : ObjectConverter {
        override fun toString(value: Any?, context: ConverterContext?): String {
            return getPath(value)
        }

        override fun supportToString(value: Any?, context: ConverterContext?): Boolean {
            return true
        }

        override fun fromString(text: String?, context: ConverterContext?): Any? {
            return text
        }

        override fun supportFromString(text: String?, context: ConverterContext?): Boolean {
            return true
        }
    }

    val palettePath: String
        get() = getPath(editor.item).trim()

    init {
        isEditable = true
        setConverter(PathConverter)
        selectedItem = initialPath

        findTextComponents(editor.editorComponent).forEach {
            it.document.addDocumentListener(SimpleDocumentListener { onChanged() })
        }
        addItemListener { onChanged() }
    }

    override fun setupTree(tree: JTree) {
        super.setupTree(tree)
        tree.isRootVisible = false
        tree.showsRootHandles = true
        var row = 0
        while (row < tree.rowCount) {
            tree.expandRow(row++)
        }
    }

    override fun isValidSelection(path: TreePath?): Boolean {
        return true
    }

    override fun convertElementToString(value: Any?): String {
        return getPath(value)
    }

    private fun findTextComponents(component: Component): List<JTextComponent> {
        return when (component) {
            is JTextComponent -> listOf(component)
            is Container -> component.components.flatMap { findTextComponents(it) }
            else -> emptyList()
        }
    }
}
