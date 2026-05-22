package com.mussonindustrial.ignition.embr.periscope.navtree

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace

@FunctionalInterface
interface NavTreeNodeFactory {
    fun createNode(
        context: DesignerContext,
        workspace: TabbedResourceWorkspace,
        resource: Resource,
    ): AbstractNavTreeNode
}
