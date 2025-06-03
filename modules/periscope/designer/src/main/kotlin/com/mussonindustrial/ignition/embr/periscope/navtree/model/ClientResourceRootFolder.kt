package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import javax.swing.Icon

class ClientResourceRootFolder(context: DesignerContext, workspace: TabbedResourceWorkspace) :
    ClientResourceFolder(context, workspace) {

    override fun getExpandedIcon(): Icon {
        return PeriscopeIcons.clientResourceFolderOpened
    }

    override fun getIcon(): Icon {
        return PeriscopeIcons.clientResourceFolderClosed
    }
}
