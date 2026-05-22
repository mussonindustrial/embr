package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.navtree.NavTreeNodeFactory
import com.mussonindustrial.ignition.embr.periscope.resources.TypeScriptResource
import javax.swing.Icon

class TypeScriptResourceNode(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    resource: Resource,
) : ClientResourceNode<TypeScriptResource>(context, workspace, resource) {

    companion object {
        val factory =
            object : NavTreeNodeFactory {
                override fun createNode(
                    context: DesignerContext,
                    workspace: TabbedResourceWorkspace,
                    resource: Resource,
                ): TypeScriptResourceNode {
                    return TypeScriptResourceNode(context, workspace, resource)
                }
            }
    }

    override fun getIcon(): Icon {
        return PeriscopeIcons.tsx
    }

    override fun getClientResource(resource: Resource): TypeScriptResource {
        return TypeScriptResource.fromResource(resource)
    }
}
