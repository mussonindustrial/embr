package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.navtree.NavTreeNodeFactory
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import javax.swing.Icon

class TypeScriptResourceNode(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    resource: ProjectResource,
) : ClientResourceNode<TypeScriptResource>(context, workspace, resource) {

    companion object {
        val factory =
            object : NavTreeNodeFactory {
                override fun createNode(
                    context: DesignerContext,
                    workspace: TabbedResourceWorkspace,
                    resource: ProjectResource,
                ): TypeScriptResourceNode {
                    return TypeScriptResourceNode(context, workspace, resource)
                }
            }
    }

    override fun getIcon(): Icon {
        return PeriscopeIcons.tsx
    }

    override fun getClientResource(resource: ProjectResource): TypeScriptResource {
        return TypeScriptResource.fromResource(resource)
    }
}
