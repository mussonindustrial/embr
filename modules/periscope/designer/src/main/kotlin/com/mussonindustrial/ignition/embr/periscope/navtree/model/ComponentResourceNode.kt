package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.navtree.NavTreeNodeFactory
import com.mussonindustrial.ignition.embr.periscope.resources.ComponentResource
import javax.swing.Icon

class ComponentResourceNode(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    resource: ProjectResource,
) : ClientResourceNode<ComponentResource>(context, workspace, resource) {

    companion object {
        val factory =
            object : NavTreeNodeFactory {
                override fun createNode(
                    context: DesignerContext,
                    workspace: TabbedResourceWorkspace,
                    resource: ProjectResource,
                ): ComponentResourceNode {
                    return ComponentResourceNode(context, workspace, resource)
                }
            }
    }

    override fun getIcon(): Icon {
        return PeriscopeIcons.component
    }

    override fun getClientResource(resource: ProjectResource): ComponentResource {
        return ComponentResource.fromResource(resource)
    }
}
