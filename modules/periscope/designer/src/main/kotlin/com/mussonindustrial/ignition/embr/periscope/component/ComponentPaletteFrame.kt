package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.inductiveautomation.ignition.client.util.gui.JideToolbarButton
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.designer.IgnitionDesigner
import com.inductiveautomation.ignition.designer.WorkspaceManager
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerHook
import com.inductiveautomation.perspective.designer.model.DropObject
import com.jidesoft.docking.DockContext
import com.jidesoft.docking.DockableFrame
import com.jidesoft.grid.QuickFilterField
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentInfo
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentLibraryInfo
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentVariantInfo
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.DropMode
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JToolBar
import javax.swing.JTree
import javax.swing.SwingUtilities.invokeLater
import javax.swing.ToolTipManager
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel
import net.miginfocom.swing.MigLayout

class ComponentPaletteFrame(
    private val designer: DesignerContext,
    private val registry: LibraryComponentRegistry,
    private val openResource: (String) -> Unit,
) : DockableFrame(DOCKING_KEY, PeriscopeIcons.tsx) {

    private val perspectiveWorkspaceKey = DesignerHook.get(designer).workspace.key
    private val navigationListener =
        object : WorkspaceManager.WorkspaceNavigationListener {
            override fun workspaceActivated(key: String) {
                if (key == perspectiveWorkspaceKey) invokeLater { showInWorkspace(true) }
            }

            override fun workspaceDeactivated(key: String) {
                if (key == perspectiveWorkspaceKey) invokeLater { showInWorkspace(false) }
            }
        }

    private val history = PaletteHistory(designer.project?.name.orEmpty())
    private val root = DefaultMutableTreeNode()
    private val model = DefaultTreeModel(root)
    private val tree =
        JTree(model).apply {
            isRootVisible = false
            showsRootHandles = true
            dragEnabled = true
            dropMode = DropMode.ON
            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            cellRenderer = ComponentTreeCellRenderer()
            transferHandler =
                ComponentTransferHandler({ selectedValue(selectionPath) }, ::recordUse)
        }
    private val cards = CardLayout()
    private val content =
        JPanel(cards).apply {
            add(JScrollPane(tree), TREE_CARD)
            add(
                JLabel(EMPTY_MESSAGE, JLabel.CENTER).apply { verticalAlignment = JLabel.TOP },
                MESSAGE_CARD,
            )
        }
    private var ready = false
    private var lastQuery: String? = null
    private var knownFolders = emptySet<String>()
    private val filter =
        object : QuickFilterField() {
            init {
                isWildcardEnabled = true
                hintText = BundleUtil.i18n("words.search")
            }

            override fun applyFilter(text: String?) {
                if (ready) rebuild()
            }
        }
    private val expandAll =
        object : AbstractAction("Expand All", VectorIcons.get("expand-all")) {
            override fun actionPerformed(event: ActionEvent) = expandFolders()
        }
    private val collapseAll =
        object : AbstractAction("Collapse All", VectorIcons.get("collapse-all")) {
            override fun actionPerformed(event: ActionEvent) = collapseRows()
        }
    private val toolbar =
        JToolBar().apply {
            layout = MigLayout("ins 4 8 4 8, gapx 4px")
            isFloatable = false
            add(filter, "push, grow")
            add(JideToolbarButton(expandAll))
            add(JideToolbarButton(collapseAll))
        }

    private val workspaceManager
        get() = (designer.frame as IgnitionDesigner).workspace

    init {
        title = TITLE
        tabTitle = TITLE
        sideTitle = TITLE
        context.initMode = DockContext.STATE_AUTOHIDE
        context.initSide = DockContext.DOCK_SIDE_EAST
        context.initIndex = INIT_INDEX

        contentPane.add(
            JPanel(BorderLayout()).apply {
                add(toolbar, BorderLayout.NORTH)
                add(content, BorderLayout.CENTER)
            },
        )

        ToolTipManager.sharedInstance().registerComponent(tree)
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(event: MouseEvent) {
                    if (event.clickCount == 2) openDefinition(selectedComponent(pathAt(event)))
                }

                override fun mousePressed(event: MouseEvent) = showPopup(event)

                override fun mouseReleased(event: MouseEvent) = showPopup(event)
            },
        )

        registry.addListener { invokeLater { rebuild() } }
        ready = true
        rebuild()
    }

    fun startup() {
        workspaceManager.addNavigationListener(navigationListener)
        invokeLater {
            val selectedKey = workspaceManager.selectedWorkspace?.key
            showInWorkspace(selectedKey == perspectiveWorkspaceKey)
        }
    }

    fun shutdown() {
        workspaceManager.removeNavigationListener(navigationListener)
    }

    private fun showInWorkspace(available: Boolean) {
        val dockingManager = designer.dockingManager
        if (dockingManager.getFrame(DOCKING_KEY) == null) return

        if (available) {
            dockingManager.setFrameAvailable(DOCKING_KEY)
        } else {
            dockingManager.hideFrame(DOCKING_KEY)
            dockingManager.setFrameUnavailable(DOCKING_KEY)
        }
    }

    private fun pathAt(event: MouseEvent): TreePath? {
        return tree.getPathForLocation(event.x, event.y)
    }

    private fun openDefinition(info: ComponentInfo?) {
        val source = info?.source ?: return
        openResource(source)
    }

    private fun showPopup(event: MouseEvent) {
        if (!event.isPopupTrigger) return

        val path = pathAt(event)
        val value = selectedValue(path)
        if (value == RECENT) {
            showRecentPopup(event)
            return
        }
        if (value is PaletteLibrary) {
            tree.selectionPath = path
            showLibraryPopup(event, value.info)
            return
        }
        val info = selectedComponent(path) ?: return
        tree.selectionPath = path

        val openItem =
            JMenuItem("Open Definition").apply {
                isEnabled = info.source != null
                addActionListener { openDefinition(info) }
            }
        val copyItem =
            JMenuItem("Copy Component Type").apply { addActionListener { copyType(info) } }
        val pinItem =
            JMenuItem(if (history.isPinned(info.id)) "Unpin" else "Pin").apply {
                addActionListener { togglePin(info) }
            }

        val popup = JPopupMenu()
        popup.add(openItem)
        popup.add(copyItem)
        popup.addSeparator()
        popup.add(pinItem)
        popup.show(tree, event.x, event.y)
    }

    private fun showRecentPopup(event: MouseEvent) {
        val clearItem =
            JMenuItem("Clear Recent").apply {
                addActionListener {
                    history.clearRecent()
                    rebuild()
                }
            }
        JPopupMenu().apply { add(clearItem) }.show(tree, event.x, event.y)
    }

    private fun showLibraryPopup(event: MouseEvent, library: ComponentLibraryInfo) {
        val openItem =
            JMenuItem("Open Library").apply { addActionListener { openResource(library.path) } }
        JPopupMenu().apply { add(openItem) }.show(tree, event.x, event.y)
    }

    private fun copyType(info: ComponentInfo) {
        val type = StringSelection(LibraryComponents.typeOf(info.id))
        Toolkit.getDefaultToolkit().systemClipboard.setContents(type, null)
    }

    private fun togglePin(info: ComponentInfo) {
        history.togglePin(info.id)
        rebuild()
    }

    private fun recordUse(info: ComponentInfo) {
        history.recordUse(info.id)
        rebuild()
    }

    private fun rebuild() {
        val query = filter.searchingText?.trim().orEmpty()
        val expanded = if (query == lastQuery) expandedKeys() else null
        lastQuery = query
        root.removeAllChildren()
        cards.show(content, if (registry.components.isEmpty()) MESSAGE_CARD else TREE_CARD)

        val libraryNames = registry.libraries.associate { it.path to it.displayName }
        val matchingComponents =
            registry.components
                .filter { matches(it, libraryNames[it.library], query) }
                .sortedBy { it.name.lowercase() }
        val componentsById = matchingComponents.associateBy { it.id }
        val hint = query.isEmpty()

        addSection(PINNED, matchingComponents.filter { history.isPinned(it.id) }, hint)
        addSection(RECENT, history.recent.mapNotNull { componentsById[it] }, hint)
        val libraryNodes = libraryNodes()
        for (info in matchingComponents) {
            val libraryNode = libraryNodes[info.library] ?: root
            folderOf(libraryNode, info).add(componentNode(info))
        }
        for (node in libraryNodes.values) {
            if (node.childCount == 0) continue
            sortFolders(node)
            root.add(node)
        }

        model.reload()
        if (expanded == null) expandFolders() else restoreExpanded(expanded)
        knownFolders = folderNodes().map { keyOf(it) }.toSet()
    }

    private fun matches(info: ComponentInfo, libraryName: String?, query: String): Boolean {
        if (query.isEmpty()) return true
        val folderNames = info.category.orEmpty().split('/').filter { it.isNotBlank() }
        val names = listOfNotNull(info.name, info.id, libraryName) + folderNames
        return names.any { filter.matches(it, query) }
    }

    private fun addSection(
        section: PaletteSection,
        components: List<ComponentInfo>,
        showHint: Boolean,
    ) {
        if (components.isEmpty() && !showHint) return
        val node = DefaultMutableTreeNode(section)
        if (components.isEmpty()) node.add(DefaultMutableTreeNode(PaletteHint(section.hint)))
        for (info in components) node.add(componentNode(info))
        root.add(node)
    }

    private fun componentNode(info: ComponentInfo): DefaultMutableTreeNode {
        val node = DefaultMutableTreeNode(info)
        for (variant in info.variants.orEmpty()) {
            node.add(DefaultMutableTreeNode(PaletteVariant(info, variant)))
        }
        return node
    }

    private fun sortFolders(node: DefaultMutableTreeNode) {
        val children =
            node.children().asSequence().filterIsInstance<DefaultMutableTreeNode>().toList()
        val (folders, components) = children.partition { it.userObject is String }
        node.removeAllChildren()
        for (folder in folders.sortedBy { it.userObject.toString().lowercase() }) {
            sortFolders(folder)
            node.add(folder)
        }
        for (component in components) node.add(component)
    }

    private fun libraryNodes(): Map<String, DefaultMutableTreeNode> {
        return registry.libraries
            .sortedBy { it.displayName.lowercase() }
            .associate { it.path to DefaultMutableTreeNode(PaletteLibrary(it)) }
    }

    private fun folderOf(
        libraryNode: DefaultMutableTreeNode,
        info: ComponentInfo,
    ): DefaultMutableTreeNode {
        val folderNames = info.category.orEmpty().split('/').filter { it.isNotBlank() }

        var folder = libraryNode
        for (name in folderNames) {
            val existing =
                folder.children().asSequence().filterIsInstance<DefaultMutableTreeNode>().find {
                    it.userObject == name
                }
            if (existing != null) {
                folder = existing
            } else {
                val created = DefaultMutableTreeNode(name)
                folder.add(created)
                folder = created
            }
        }
        return folder
    }

    private fun nodes(): Sequence<DefaultMutableTreeNode> {
        return root
            .breadthFirstEnumeration()
            .asSequence()
            .filterIsInstance<DefaultMutableTreeNode>()
    }

    private fun folderNodes(): Sequence<DefaultMutableTreeNode> {
        return nodes().filter {
            it.userObject is String ||
                it.userObject is PaletteSection ||
                it.userObject is PaletteLibrary
        }
    }

    private fun keyOf(node: DefaultMutableTreeNode): String {
        return node.path.drop(1).joinToString("/") {
            when (val value = (it as DefaultMutableTreeNode).userObject) {
                is ComponentInfo -> value.id
                is PaletteSection -> "#${value.name}"
                is PaletteLibrary -> "@${value.info.path}"
                else -> value.toString()
            }
        }
    }

    private fun expandedKeys(): Set<String> {
        val expanded = tree.getExpandedDescendants(TreePath(root)) ?: return emptySet()
        return expanded
            .asSequence()
            .map { keyOf(it.lastPathComponent as DefaultMutableTreeNode) }
            .toSet()
    }

    private fun restoreExpanded(expanded: Set<String>) {
        val folders = folderNodes().toSet()
        for (node in nodes().filter { it.childCount > 0 }) {
            val key = keyOf(node)
            val isNewFolder = node in folders && key !in knownFolders
            if (key in expanded || isNewFolder) tree.expandPath(TreePath(node.path))
        }
    }

    private fun expandFolders() {
        for (node in folderNodes()) tree.expandPath(TreePath(node.path))
    }

    private fun collapseRows() {
        for (row in tree.rowCount - 1 downTo 0) tree.collapseRow(row)
    }

    private fun selectedValue(path: TreePath?): Any? {
        val node = path?.lastPathComponent as? DefaultMutableTreeNode
        return node?.userObject
    }

    private fun selectedComponent(path: TreePath?): ComponentInfo? {
        return when (val value = selectedValue(path)) {
            is ComponentInfo -> value
            is PaletteVariant -> value.info
            else -> null
        }
    }

    private class PaletteVariant(val info: ComponentInfo, val variant: ComponentVariantInfo)

    private class PaletteSection(val name: String, val icon: String, val hint: String)

    private class PaletteHint(val text: String)

    private class PaletteLibrary(val info: ComponentLibraryInfo)

    private class ComponentTreeCellRenderer : DefaultTreeCellRenderer() {

        override fun getTreeCellRendererComponent(
            tree: JTree,
            value: Any?,
            selected: Boolean,
            expanded: Boolean,
            leaf: Boolean,
            row: Int,
            hasFocus: Boolean,
        ): Component {
            val item = (value as? DefaultMutableTreeNode)?.userObject
            val label = item as? String ?: ""
            super.getTreeCellRendererComponent(tree, label, selected, expanded, leaf, row, hasFocus)

            when (item) {
                is PaletteLibrary -> renderLibrary(item)
                is PaletteHint -> renderHint(item, selected)
                is PaletteSection -> renderSection(item)
                is PaletteVariant -> renderVariant(item)
                is ComponentInfo -> renderComponent(item, selected)
            }
            return this
        }

        private fun renderHint(item: PaletteHint, selected: Boolean) {
            text = item.text
            icon = null
            toolTipText = null
            if (!selected) foreground = HINT_COLOR
        }

        private fun renderLibrary(item: PaletteLibrary) {
            text = item.info.displayName
            icon = ComponentIcons.library(item.info.path)
            toolTipText = item.info.path
        }

        private fun renderSection(item: PaletteSection) {
            text = item.name
            icon = VectorIcons.get(item.icon)
            toolTipText = null
        }

        private fun renderVariant(item: PaletteVariant) {
            text = item.variant.name
            icon = ComponentIcons.get(item.info.id)
            val tooltipLines =
                listOfNotNull("${item.info.name} - ${item.variant.name}", item.variant.description)
            toolTipText = tooltipLines.joinToString("<br>", "<html>", "</html>")
        }

        private fun renderComponent(info: ComponentInfo, selected: Boolean) {
            val diagnostics = diagnosticsOf(info)
            text = if (diagnostics.isEmpty()) info.name else "${info.name}  (!)"
            icon = ComponentIcons.get(info.id)

            val tooltipLines = listOfNotNull(info.description, info.id, info.source) + diagnostics
            toolTipText = tooltipLines.joinToString("<br>", "<html>", "</html>")

            if (diagnostics.isNotEmpty() && !selected) foreground = WARNING_COLOR
        }

        private fun diagnosticsOf(info: ComponentInfo): List<String> {
            val diagnostics = info.diagnostics.orEmpty().toMutableList()
            if (info.icon != null && ComponentIcons.get(info.id).missing) {
                diagnostics += "Icon '${info.icon}' not found."
            }
            return diagnostics
        }
    }

    private class ComponentTransferHandler(
        private val selection: () -> Any?,
        private val onDropped: (ComponentInfo) -> Unit,
    ) : TransferHandler() {

        override fun getSourceActions(c: JComponent) = COPY

        override fun createTransferable(c: JComponent): Transferable? {
            return when (val value = selection()) {
                is ComponentInfo -> transferableOf(value, null)
                is PaletteVariant -> transferableOf(value.info, value.variant)
                else -> null
            }
        }

        override fun exportDone(source: JComponent, data: Transferable?, action: Int) {
            if (action == NONE) return
            when (val value = selection()) {
                is ComponentInfo -> onDropped(value)
                is PaletteVariant -> onDropped(value.info)
            }
        }

        private fun transferableOf(
            info: ComponentInfo,
            variant: ComponentVariantInfo?,
        ): Transferable {
            val size = variant?.defaultSize ?: info.defaultSize
            val payload =
                JsonObject().apply {
                    addProperty("type", LibraryComponents.typeOf(info.id))
                    val name = info.name + variant?.name.orEmpty()
                    addProperty("name", name.filter(Char::isLetterOrDigit))
                    add("props", variant?.props ?: info.defaults)
                    if (size != null) {
                        addProperty("width", size.width)
                        addProperty("height", size.height)
                    }
                }
            return DropObject.createTransferable(DROP_TYPE, payload)
        }
    }

    companion object {
        private const val DOCKING_KEY = "embr-periscope-component-palette"
        private const val DROP_TYPE = "embr-periscope-library-component"
        private const val TITLE = "React Components"
        private const val INIT_INDEX = 12
        private const val TREE_CARD = "tree"
        private const val MESSAGE_CARD = "message"
        private const val EMPTY_MESSAGE =
            "<html><center><br>No components loaded.<br><br>" +
                "Define components with defineComponent,<br>" +
                "then list them in a Component Library.</center></html>"
        private val WARNING_COLOR = Color(0xC2, 0x41, 0x0C)
        private val HINT_COLOR = Color.GRAY
        private val PINNED =
            PaletteSection("Pinned", "frame-pin", "Right-click a component to pin it")
        private val RECENT =
            PaletteSection("Recent", "history", "Components you add to views appear here")
    }
}
