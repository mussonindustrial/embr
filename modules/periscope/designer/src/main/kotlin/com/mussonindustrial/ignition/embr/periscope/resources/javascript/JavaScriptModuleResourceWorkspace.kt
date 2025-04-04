package com.mussonindustrial.ignition.embr.periscope.resources.javascript

import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.Project
import com.inductiveautomation.ignition.common.project.resource.ProjectResourceBuilder
import com.inductiveautomation.ignition.common.project.resource.ResourcePath
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.icon.InteractiveSvgIcon
import com.inductiveautomation.ignition.designer.navtree.model.MutableNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.*
import com.inductiveautomation.ignition.designer.workspacewelcome.RecentlyModifiedTablePanel
import com.inductiveautomation.ignition.designer.workspacewelcome.ResourceBuilderDelegate
import com.inductiveautomation.ignition.designer.workspacewelcome.ResourceBuilderPanel
import com.inductiveautomation.ignition.designer.workspacewelcome.WorkspaceWelcomePanel
import com.mussonindustrial.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.periscope.Meta
import com.mussonindustrial.ignition.embr.periscope.resources.JavaScriptModule
import java.util.*
import java.util.function.Consumer
import javax.swing.JComponent
import javax.swing.JPopupMenu
import org.json.JSONException

class JavaScriptModuleResourceWorkspace(
    private val context: DesignerContext,
    private val parent: MutableNavTreeNode,
) :
    TabbedResourceWorkspace(
        context,
        ResourceDescriptor.builder()
            .resourceType(JavaScriptModule.RESOURCE_TYPE)
            .rootFolderText(BundleUtil.i18n("periscope.javascript-module.nouns"))
            .nounKey("periscope.javascript-module.noun")
            .rootIcon(InteractiveSvgIcon(Meta::class.java, "images/svgicons/schema-folder.svg"))
            .icon(InteractiveSvgIcon(Meta::class.java, "images/svgicons/schema.svg"))
            .navTreeLocation(9999999)
            .scope(ApplicationScope.GATEWAY)
            .build(),
    ) {

    private val logger = this.getLogger()

    private fun getNewConstructor(project: Project): Consumer<ProjectResourceBuilder> {
        return Consumer<ProjectResourceBuilder> { builder: ProjectResourceBuilder ->
            try {
                builder.putData(JavaScriptModule.DATA_KEY, ByteArray(2))
            } catch (e: JSONException) {
                logger.error("Error creating new JavaScript Module.", e)
            }
        }
    }

    override fun getKey(): String {
        return "javascript-module"
    }

    public override fun getNavTreeNodeParent(): MutableNavTreeNode {
        return this.parent
    }

    override fun newResourceEditor(resourcePath: ResourcePath): ResourceEditor<*> {
        return JavaScriptResourceEditor(this, resourcePath)
    }

    override fun addNewResourceActions(folderNode: ResourceFolderNode, menu: JPopupMenu) {
        menu.add(
            object : NewResourceAction(this, folderNode, getNewConstructor(context.project!!)) {
                init {
                    putValue(NAME, BundleUtil.i18n("periscope.javascript-module.action.new"))
                    putValue(
                        SMALL_ICON,
                        InteractiveSvgIcon(Meta::class.java, "images/svgicons/theme.svg"),
                    )
                }

                override fun newResourceName(): String {
                    return BundleUtil.i18n("periscope.javascript-module.action.new.defaultName")
                }
            }
        )
    }

    override fun createWorkspaceHomeTab(): Optional<JComponent> {
        val ws = this

        return Optional.of<JComponent>(
            object :
                WorkspaceWelcomePanel(BundleUtil.i18n("periscope.javascript-module.nouns-long")) {
                override fun createPanels(): List<JComponent> {
                    return listOf<JComponent>(
                        ResourceBuilderPanel(
                            context,
                            BundleUtil.i18n("periscope.javascript-module.noun"),
                            JavaScriptModule.RESOURCE_TYPE.rootPath(),
                            listOf<ResourceBuilderDelegate>(
                                ResourceBuilderDelegate.build(
                                    BundleUtil.i18n("periscope.javascript-module.noun-long"),
                                    InteractiveSvgIcon(
                                        Meta::class.java,
                                        "images/svgicons/theme.svg",
                                    ),
                                    getNewConstructor(context.project!!),
                                )
                            ),
                        ) { path: ResourcePath? ->
                            ws.open(path)
                        },
                        RecentlyModifiedTablePanel(
                            context,
                            JavaScriptModule.RESOURCE_TYPE,
                            BundleUtil.i18n("periscope.javascript-module.nouns-long"),
                        ) { path: ResourcePath? ->
                            ws.open(path)
                        },
                    )
                }
            }
        )
    }
}
