package com.mussonindustrial.embr.designer.navtree.model

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.tree.TreePath

abstract class HiddenActionResourceNode(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    resource: Resource,
) : ResourceNode(context, workspace, resource) {

    fun JPopupMenu.item(text: String, action: (JMenuItem) -> Unit): JMenuItem {
        val item = JMenuItem(text)
        item.addActionListener { action(item) }
        add(item)
        return item
    }

    override fun initPopupMenu(
        menu: JPopupMenu,
        paths: Array<TreePath>,
        selection: List<AbstractNavTreeNode>,
        modifiers: Int,
    ) {
        super.initPopupMenu(menu, paths, selection, modifiers)
        if ((modifiers and SHIFT_MASK) == SHIFT_MASK) {
            addShiftClickMenuItems(menu, paths, selection, modifiers)
        }
    }

    abstract fun addShiftClickMenuItems(
        menu: JPopupMenu,
        paths: Array<TreePath>,
        selection: List<AbstractNavTreeNode>,
        modifiers: Int,
    )

    companion object {
        const val SHIFT_MASK: Int = 64
    }
}
