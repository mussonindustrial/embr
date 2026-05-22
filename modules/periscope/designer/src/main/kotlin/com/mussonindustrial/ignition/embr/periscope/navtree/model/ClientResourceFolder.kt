package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceFolderNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.resources.DesignerClientResourceDescriptor

open class ClientResourceFolder : ResourceFolderNode {
    constructor(
        context: DesignerContext,
        workspace: TabbedResourceWorkspace,
    ) : super(context, workspace)

    constructor(
        context: DesignerContext,
        workspace: TabbedResourceWorkspace,
        resource: Resource,
    ) : super(context, workspace, resource)

    override fun createChildNode(resource: Resource): AbstractNavTreeNode? {
        if (resource.isFolder) {
            return ClientResourceFolder(context, workspace, resource)
        }

        val descriptor =
            PeriscopeDesignerContext.instance.clientResourceManager.getDescriptor(resource)
                as? DesignerClientResourceDescriptor?
        if (descriptor == null) return null

        return descriptor.navTreeNodeFactory.createNode(context, workspace, resource)
    }
}
