package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.inductiveautomation.ignition.designer.workspacewelcome.RecentlyModifiedTablePanel
import com.inductiveautomation.ignition.designer.workspacewelcome.ResourceBuilderDelegate
import com.inductiveautomation.ignition.designer.workspacewelcome.ResourceBuilderPanel
import com.inductiveautomation.ignition.designer.workspacewelcome.WorkspaceWelcomePanel
import javax.swing.JComponent

class ClientResourceWorkspaceHomeTab(
    val context: DesignerContext,
    val clientResourceManager: ClientResourceManager,
    val workspace: TabbedResourceWorkspace,
) : WorkspaceWelcomePanel(BundleUtil.i18n("periscope.client-resource.nouns-long")) {

    override fun createPanels(): List<JComponent> {
        return listOf<JComponent>(
            ResourceBuilderPanel(
                context,
                BundleUtil.i18n("periscope.client-resource.noun"),
                ClientResource.type.rootPath(),
                clientResourceManager.descriptors.map {
                    ResourceBuilderDelegate.build(
                        it.nounLongKey,
                        it.icon,
                        it.createEmpty()::applyToBuilder,
                    )
                },
                workspace::open,
            ),
            RecentlyModifiedTablePanel(
                context,
                ClientResource.type,
                BundleUtil.i18n("periscope.client-resource.nouns-long"),
                workspace::open,
            ),
        )
    }
}
