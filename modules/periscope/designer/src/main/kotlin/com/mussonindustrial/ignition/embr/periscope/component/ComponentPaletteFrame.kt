package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.designer.IgnitionDesigner
import com.inductiveautomation.ignition.designer.WorkspaceManager
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerHook
import com.inductiveautomation.perspective.designer.model.DropObject
import com.jidesoft.docking.DockContext
import com.jidesoft.docking.DockableFrame
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.editor.SimpleDocumentListener
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentInfo
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentVariantInfo
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Component
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.DropMode
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.JTree
import javax.swing.SwingUtilities.invokeLater
import javax.swing.ToolTipManager
import javax.swing.TransferHandler
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

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

    private val root = DefaultMutableTreeNode()
    private val model = DefaultTreeModel(root)
    private val filter = JTextField()
    private val tree =
        JTree(model).apply {
            isRootVisible = false
            showsRootHandles = true
            dragEnabled = true
            dropMode = DropMode.ON
            selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
            cellRenderer = ComponentTreeCellRenderer()
            transferHandler = ComponentTransferHandler { selectedValue(selectionPath) }
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
            JPanel(BorderLayout(0, 4)).apply {
                add(filter, BorderLayout.NORTH)
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

        filter.document.addDocumentListener(SimpleDocumentListener { rebuild() })
        registry.addListener { invokeLater { rebuild() } }
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
        val info = selectedComponent(path) ?: return
        tree.selectionPath = path

        val openItem =
            JMenuItem("Open Definition").apply {
                isEnabled = info.source != null
                addActionListener { openDefinition(info) }
            }
        val copyItem =
            JMenuItem("Copy Component Type").apply { addActionListener { copyType(info) } }

        val popup = JPopupMenu()
        popup.add(openItem)
        popup.add(copyItem)
        popup.show(tree, event.x, event.y)
    }

    private fun copyType(info: ComponentInfo) {
        val type = StringSelection(LibraryComponents.typeOf(info.id))
        Toolkit.getDefaultToolkit().systemClipboard.setContents(type, null)
    }

    private fun rebuild() {
        val query = filter.text.trim().lowercase()
        root.removeAllChildren()
        cards.show(content, if (registry.components.isEmpty()) MESSAGE_CARD else TREE_CARD)

        val matchingComponents =
            registry.components
                .filter { query.isEmpty() || query in "${it.name} ${it.id}".lowercase() }
                .sortedBy { it.name.lowercase() }

        for (info in matchingComponents) {
            val node = DefaultMutableTreeNode(info)
            for (variant in info.variants.orEmpty()) {
                node.add(DefaultMutableTreeNode(PaletteVariant(info, variant)))
            }
            folderOf(info).add(node)
        }

        model.reload()
        var row = 0
        while (row < tree.rowCount) tree.expandRow(row++)
    }

    private fun folderOf(info: ComponentInfo): DefaultMutableTreeNode {
        val folderNames = info.category.orEmpty().split('/').filter { it.isNotBlank() }

        var folder = root
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
                is PaletteVariant -> renderVariant(item)
                is ComponentInfo -> renderComponent(item, selected)
            }
            return this
        }

        private fun renderVariant(item: PaletteVariant) {
            text = item.variant.name
            icon = ComponentIcons.get(item.info.id)
            toolTipText = item.variant.description ?: item.info.name
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

    private class ComponentTransferHandler(private val selection: () -> Any?) : TransferHandler() {

        override fun getSourceActions(c: JComponent) = COPY

        override fun createTransferable(c: JComponent): Transferable? {
            return when (val value = selection()) {
                is ComponentInfo -> transferableOf(value, null)
                is PaletteVariant -> transferableOf(value.info, value.variant)
                else -> null
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
                    addProperty("name", info.name.filter(Char::isLetterOrDigit))
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
    }
}
