package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.tree.TreePath

class JavaScriptModuleResourceNode(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    resource: ProjectResource,
) : HiddenActionResourceNode(context, workspace, resource) {
    override fun addShiftClickMenuItems(
        menu: JPopupMenu,
        paths: Array<TreePath>,
        selection: List<AbstractNavTreeNode>,
        modifiers: Int,
    ) {

        val copyJavaScript = JMenuItem("Copy JavaScript")
        val pasteJavaScript = JMenuItem("Paste JavaScript")
        menu.addSeparator()
        menu.add(copyJavaScript)
        menu.add(pasteJavaScript)
    }
}
