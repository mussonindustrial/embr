package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.client.icons.VectorIcons
import com.inductiveautomation.ignition.client.util.gui.JideToolbarButton
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.designer.IgnitionDesigner
import com.inductiveautomation.ignition.designer.WorkspaceManager
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.perspective.designer.DesignerHook
import com.jidesoft.docking.DockContext
import com.jidesoft.docking.DockableFrame
import com.jidesoft.grid.QuickFilterField
import com.mussonindustrial.ignition.embr.periscope.i18n
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.editor.components.ScriptEditor.Companion.menuItem
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractAction
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.JScrollPane
import javax.swing.JToolBar
import javax.swing.SwingUtilities.invokeLater
import javax.swing.tree.TreePath
import net.miginfocom.swing.MigLayout

class ComponentPaletteFrame(
    private val designerContext: DesignerContext,
    private val registry: ProjectComponentRegistry,
    private val openResource: (String) -> Unit,
) : DockableFrame(DOCKING_KEY, PeriscopeIcons.component) {

    companion object {
        private const val DOCKING_KEY = "embr-periscope-component-palette"
        private const val INIT_INDEX = 12
        private const val TREE_CARD = "tree"
        private const val MESSAGE_CARD = "message"
    }

    private val perspectiveWorkspaceKey = DesignerHook.get(designerContext).workspace.key
    private val navigationListener =
        object : WorkspaceManager.WorkspaceNavigationListener {
            override fun workspaceActivated(key: String) {
                if (key == perspectiveWorkspaceKey) {
                    invokeLater { showInWorkspace(true) }
                }
            }

            override fun workspaceDeactivated(key: String) {
                if (key == perspectiveWorkspaceKey) {
                    invokeLater { showInWorkspace(false) }
                }
            }
        }

    private val history = PaletteHistory(designerContext.project?.name.orEmpty())
    private val tree = PaletteTree(registry, history) { text, query -> filter.matches(text, query) }
    private val cards = CardLayout()
    private val content =
        JPanel(cards).apply {
            add(JScrollPane(tree), TREE_CARD)
            add(
                JLabel(emptyText(), JLabel.CENTER).apply {
                    verticalAlignment = JLabel.TOP
                },
                MESSAGE_CARD,
            )
        }
    private val filter =
        object : QuickFilterField() {
            init {
                isWildcardEnabled = true
                hintText = BundleUtil.i18n("words.search")
            }

            override fun applyFilter(text: String?) {
                if (ready) {
                    rebuild()
                }
            }
        }
    private val expandAll =
        object : AbstractAction(i18n("palette.expand-all"), VectorIcons.get("expand-all")) {
            override fun actionPerformed(e: ActionEvent) {
                tree.expandAll()
            }
        }
    private val collapseAll =
        object : AbstractAction(i18n("palette.collapse-all"), VectorIcons.get("collapse-all")) {
            override fun actionPerformed(e: ActionEvent) {
                tree.collapseAll()
            }
        }
    private val toolbar =
        JToolBar().apply {
            layout = MigLayout("ins 4 8 4 8, gapx 4")
            isFloatable = false
            add(filter, "push, grow")
            add(JideToolbarButton(expandAll))
            add(JideToolbarButton(collapseAll))
        }

    private var ready = false

    private val workspaceManager
        get() = (designerContext.frame as IgnitionDesigner).workspace

    init {
        title = i18n("palette.title")
        tabTitle = title
        sideTitle = title
        context.initMode = DockContext.STATE_AUTOHIDE
        context.initSide = DockContext.DOCK_SIDE_EAST
        context.initIndex = INIT_INDEX

        contentPane.add(
            JPanel(BorderLayout()).apply {
                add(toolbar, BorderLayout.NORTH)
                add(content, BorderLayout.CENTER)
            },
        )

        tree.transferHandler = PaletteTransferHandler(tree, ::recordUse)
        tree.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (e.clickCount == 2) {
                        openDefinition(tree.componentAt(pathAt(e)))
                    }
                }

                override fun mousePressed(e: MouseEvent) {
                    showPopup(e)
                }

                override fun mouseReleased(e: MouseEvent) {
                    showPopup(e)
                }
            },
        )

        registry.addListener(::rebuild)
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
        val dockingManager = designerContext.dockingManager
        if (dockingManager.getFrame(DOCKING_KEY) == null) {
            return
        }

        if (available) {
            dockingManager.setFrameAvailable(DOCKING_KEY)
        } else {
            dockingManager.hideFrame(DOCKING_KEY)
            dockingManager.setFrameUnavailable(DOCKING_KEY)
        }
    }

    private fun pathAt(e: MouseEvent): TreePath? {
        return tree.getPathForLocation(e.x, e.y)
    }

    private fun openDefinition(info: ComponentInfo?) {
        if (info != null) {
            openResource(info.path)
        }
    }

    private fun showPopup(e: MouseEvent) {
        if (!e.isPopupTrigger) {
            return
        }

        val path = pathAt(e)
        when (tree.valueAt(path)) {
            tree.recent -> showRecentPopup(e)
            else -> {
                val info = tree.componentAt(path) ?: return
                tree.selectionPath = path
                showComponentPopup(e, info)
            }
        }
    }

    private fun showComponentPopup(e: MouseEvent, info: ComponentInfo) {
        val pinKey = if (history.isPinned(info.id)) "palette.unpin" else "palette.pin"

        JPopupMenu()
            .apply {
                add(menuItem(i18n("palette.open-definition"), null) { openDefinition(info) })
                add(menuItem(i18n("palette.copy-type"), null) { copyType(info) })
                addSeparator()
                add(menuItem(i18n(pinKey), null) { togglePin(info) })
            }
            .show(tree, e.x, e.y)
    }

    private fun showRecentPopup(e: MouseEvent) {
        JPopupMenu()
            .apply { add(menuItem(i18n("palette.clear-recent"), null, ::clearRecent)) }
            .show(tree, e.x, e.y)
    }

    private fun emptyText(): String {
        val message = i18n("palette.empty")
        val hint = i18n("palette.empty.hint")
        return "<html><center><br>$message<br><br>$hint</center></html>"
    }

    private fun copyType(info: ComponentInfo) {
        val type = StringSelection(ProjectComponents.getType(info.id))
        Toolkit.getDefaultToolkit().systemClipboard.setContents(type, null)
    }

    private fun togglePin(info: ComponentInfo) {
        history.togglePin(info.id)
        rebuild()
    }

    private fun clearRecent() {
        history.clearRecent()
        rebuild()
    }

    private fun recordUse(info: ComponentInfo) {
        history.recordUse(info.id)
        rebuild()
    }

    private fun rebuild() {
        val empty = registry.components.none { it.attributes.isInPalette }
        cards.show(content, if (empty) MESSAGE_CARD else TREE_CARD)
        tree.rebuild(filter.searchingText?.trim().orEmpty())
    }
}
