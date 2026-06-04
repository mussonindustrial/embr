package com.mussonindustrial.ignition.embr.periscope.resources

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.navtree.model.MutableNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.*
import com.inductiveautomation.ignition.designer.workspacewelcome.RecentlyModifiedTablePanel
import com.inductiveautomation.ignition.designer.workspacewelcome.ResourceBuilderDelegate
import com.inductiveautomation.ignition.designer.workspacewelcome.ResourceBuilderPanel
import com.inductiveautomation.ignition.designer.workspacewelcome.WorkspaceWelcomePanel
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.navtree.model.ClientResourceRootFolder
import java.util.*
import javax.swing.JComponent
import javax.swing.JPopupMenu

class ClientResourceWorkspace(
    context: DesignerContext,
    private val parent: MutableNavTreeNode,
    private val clientResourceManager: ClientResourceManager,
) :
    TabbedResourceWorkspace(
        context,
        ResourceDescriptor.builder()
            .resourceType(ClientResource.type)
            .rootFolderText("Resources")
            .nounKey("periscope.client-resource.noun")
            .rootIcon(PeriscopeIcons.clientResourceFolderClosed)
            .navTreeLocation(9999999)
            .scope(ApplicationScope.GATEWAY)
            .build(),
    ) {

    override fun getKey(): String {
        return ClientResource.type.typeId
    }

    public override fun getNavTreeNodeParent(): MutableNavTreeNode {
        return this.parent
    }

    override fun createRootNavTreeNode(): AbstractNavTreeNode {
        return ClientResourceRootFolder(context, this)
    }

    override fun newResourceEditor(resourcePath: ResourcePath): ResourceEditor<*> {
        val project = context.project
        requireNotNull(project) { "No project reference!" }

        val resource = project.getResource(resourcePath).orElse(null)
        requireNotNull(resource) { "Resource $resourcePath not found!" }

        val descriptor =
            clientResourceManager.getDescriptor(resource) as DesignerClientResourceDescriptor
        requireNotNull(descriptor) { "Resource $resourcePath has no descriptor!" }

        return descriptor.resourceEditorFactory.createResourceEditor(this, resourcePath)
    }

    override fun createWorkspaceHomeTab(): Optional<JComponent> {
        return Optional.of<JComponent>(
            object :
                WorkspaceWelcomePanel(BundleUtil.i18n("periscope.client-resource.nouns-long")) {
                override fun createPanels(): List<JComponent> {
                    return listOf<JComponent>(
                        ResourceBuilderPanel(
                            context,
                            BundleUtil.i18n("periscope.client-resource.noun"),
                            ClientResource.type.rootPath(),
                            clientResourceManager.descriptors.map {
                                it.asResourceBuilderDelegate()
                            },
                            this@ClientResourceWorkspace::open,
                        ),
                        RecentlyModifiedTablePanel(
                            context,
                            ClientResource.type,
                            BundleUtil.i18n("periscope.client-resource.nouns-long"),
                            this@ClientResourceWorkspace::open,
                        ),
                    )
                }
            }
        )
    }

    fun ClientResourceDescriptor<*>.asResourceBuilderDelegate(): ResourceBuilderDelegate =
        ResourceBuilderDelegate.build(nounLongKey, icon, createEmpty()::applyToBuilder)

    class NewClientResourceAction(
        workspace: TabbedResourceWorkspace,
        folder: ResourceFolderNode,
        val definition: ClientResourceDescriptor<*>,
    ) : NewResourceAction(workspace, folder, definition.createEmpty()::applyToBuilder) {
        init {
            putValue(NAME, "New ${BundleUtil.i18n(definition.nounLongKey)}")
            putValue(SMALL_ICON, definition.icon)
        }

        override fun newResourceName() = definition.defaultResourceName
    }

    override fun addNewResourceActions(folderNode: ResourceFolderNode, menu: JPopupMenu) {
        clientResourceManager.descriptors.forEach {
            menu.add(NewClientResourceAction(this, folderNode, it))
        }
    }
}
