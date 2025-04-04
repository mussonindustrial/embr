package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.ResourceFolderNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace

class JavaScriptModuleResourceFolder : ResourceFolderNode {
    constructor(
        context: DesignerContext,
        workspace: TabbedResourceWorkspace,
    ) : super(context, workspace)

    constructor(
        context: DesignerContext,
        workspace: TabbedResourceWorkspace,
        resource: ProjectResource,
    ) : super(context, workspace, resource)

    override fun createChildNode(resource: ProjectResource): AbstractNavTreeNode? {
        return if (workspace.descriptor.resourceType == resource.resourceType) {
            if (resource.isFolder)
                JavaScriptModuleResourceFolder(this.context, this.workspace, resource)
            else JavaScriptModuleResourceNode(this.context, this.workspace, resource)
        } else {
            null
        }
    }
}
