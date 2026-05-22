package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.navtree.NavTreeNodeFactory
import com.mussonindustrial.ignition.embr.periscope.resources.CssModuleResource
import javax.swing.Icon

class CssModuleResourceNode(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    resource: Resource,
) : ClientResourceNode<CssModuleResource>(context, workspace, resource) {

    companion object {
        val factory =
            object : NavTreeNodeFactory {
                override fun createNode(
                    context: DesignerContext,
                    workspace: TabbedResourceWorkspace,
                    resource: Resource,
                ): CssModuleResourceNode {
                    return CssModuleResourceNode(context, workspace, resource)
                }
            }
    }

    override fun getIcon(): Icon {
        return PeriscopeIcons.css
    }

    override fun getClientResource(resource: Resource): CssModuleResource {
        return CssModuleResource.fromResource(resource)
    }
}
