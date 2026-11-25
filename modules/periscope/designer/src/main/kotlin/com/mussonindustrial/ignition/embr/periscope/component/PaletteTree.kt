package com.mussonindustrial.ignition.embr.periscope.component

import com.mussonindustrial.ignition.embr.periscope.i18n
import javax.swing.DropMode
import javax.swing.JTree
import javax.swing.ToolTipManager
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class PaletteTree(
    private val registry: ProjectComponentRegistry,
    private val history: PaletteHistory,
    private val matchesText: (text: String, query: String) -> Boolean,
) : JTree() {

    val recent = PaletteSection(i18n("palette.recent"), "history", i18n("palette.recent.hint"))

    private val root = DefaultMutableTreeNode()
    private val nodeModel = DefaultTreeModel(root)
    private val pinned =
        PaletteSection(i18n("palette.pinned"), "frame-pin", i18n("palette.pinned.hint"))
    private var lastQuery: String? = null
    private var knownFolders = emptySet<String>()

    val selectedValue: Any?
        get() = valueAt(selectionPath)

    val selectedComponent: ComponentInfo?
        get() = componentAt(selectionPath)

    init {
        model = nodeModel
        isRootVisible = false
        showsRootHandles = true
        dragEnabled = true
        dropMode = DropMode.ON
        selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        cellRenderer = PaletteCellRenderer()
        ToolTipManager.sharedInstance().registerComponent(this)
    }

    fun valueAt(path: TreePath?): Any? {
        val node = path?.lastPathComponent as? DefaultMutableTreeNode
        return node?.userObject
    }

    fun componentAt(path: TreePath?): ComponentInfo? {
        return when (val value = valueAt(path)) {
            is ComponentInfo -> value
            is PaletteVariant -> value.info
            else -> null
        }
    }

    fun rebuild(query: String) {
        val expanded = if (query == lastQuery) expandedKeys() else null
        lastQuery = query

        root.removeAllChildren()

        val matchingComponents =
            registry.components
                .filter { it.attributes.isInPalette && matches(it, query) }
                .sortedBy { it.name.lowercase() }
        val componentsById = matchingComponents.associateBy { it.id }
        val showHints = query.isEmpty()

        addSection(pinned, matchingComponents.filter { history.isPinned(it.id) }, showHints)
        addSection(recent, history.recent.mapNotNull { componentsById[it] }, showHints)
        addFolders(matchingComponents)
        nodeModel.reload()

        if (expanded == null) {
            expandAll()
        } else {
            restoreExpanded(expanded)
        }
        knownFolders = folderNodes().map { createKey(it) }.toSet()
    }

    fun expandAll() {
        folderNodes().forEach { expandPath(TreePath(it.path)) }
    }

    fun collapseAll() {
        (rowCount - 1 downTo 0).forEach { collapseRow(it) }
    }

    private fun matches(info: ComponentInfo, query: String): Boolean {
        if (query.isEmpty()) {
            return true
        }
        val names = listOf(info.name, info.id) + info.attributes.paletteFolders
        return names.any { matchesText(it, query) }
    }

    private fun addSection(
        section: PaletteSection,
        components: List<ComponentInfo>,
        showHint: Boolean,
    ) {
        if (components.isEmpty() && !showHint) {
            return
        }
        val node = DefaultMutableTreeNode(section)
        if (components.isEmpty()) {
            node.add(DefaultMutableTreeNode(PaletteHint(section.hint)))
        }
        components.forEach { node.add(componentNode(it)) }
        root.add(node)
    }

    private fun addFolders(components: List<ComponentInfo>) {
        val folders = DefaultMutableTreeNode()
        components.forEach { folderFor(folders, it).add(componentNode(it)) }

        sortFolders(folders)
        folders.childNodes().forEach { root.add(it) }
    }

    private fun componentNode(info: ComponentInfo): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(info)
        info.definition.variants.orEmpty().forEach {
            node.add(DefaultMutableTreeNode(PaletteVariant(info, it)))
        }
        return node
    }

    private fun folderFor(
        parent: DefaultMutableTreeNode,
        info: ComponentInfo,
    ): DefaultMutableTreeNode {
        val folders = info.attributes.paletteFolders.map { PaletteFolder(it) }
        return folders.fold(parent) { node, folder ->
            node.childNodes().find { it.userObject == folder } ?: node.addChild(folder)
        }
    }

    private fun sortFolders(node: DefaultMutableTreeNode) {
        val (folders, components) = node.childNodes().partition { it.userObject is PaletteFolder }
        node.removeAllChildren()
        folders
            .sortedBy { (it.userObject as PaletteFolder).name.lowercase() }
            .forEach { folder ->
                sortFolders(folder)
                node.add(folder)
            }
        components.forEach { node.add(it) }
    }

    private fun nodes(): Sequence<DefaultMutableTreeNode> {
        return root
            .breadthFirstEnumeration()
            .asSequence()
            .filterIsInstance<DefaultMutableTreeNode>()
    }

    private fun folderNodes(): Sequence<DefaultMutableTreeNode> {
        return nodes().filter { it.userObject is PaletteFolder || it.userObject is PaletteSection }
    }

    private fun createKey(node: DefaultMutableTreeNode): String {
        return node.path.drop(1).joinToString("/") {
            when (val value = (it as DefaultMutableTreeNode).userObject) {
                is ComponentInfo -> value.id
                is PaletteSection -> "#${value.name}"
                is PaletteFolder -> value.name
                is PaletteVariant -> value.variant.name
                else -> ""
            }
        }
    }

    private fun expandedKeys(): Set<String> {
        val expanded = getExpandedDescendants(TreePath(root)) ?: return emptySet()
        return expanded
            .asSequence()
            .map { createKey(it.lastPathComponent as DefaultMutableTreeNode) }
            .toSet()
    }

    private fun restoreExpanded(expanded: Set<String>) {
        val folders = folderNodes().toSet()
        nodes()
            .filter { it.childCount > 0 }
            .forEach { node ->
                val key = createKey(node)
                val newFolder = node in folders && key !in knownFolders
                if (key in expanded || newFolder) {
                    expandPath(TreePath(node.path))
                }
            }
    }
}
