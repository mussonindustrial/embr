package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.tabbedworkspace.TabbedResourceWorkspace
import com.mussonindustrial.embr.designer.navtree.model.HiddenActionResourceNode
import com.mussonindustrial.ignition.embr.periscope.PeriscopeDesignerContext
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.swing.JPopupMenu
import javax.swing.tree.TreePath

abstract class ClientResourceNode<out T : ClientResource>(
    context: DesignerContext,
    workspace: TabbedResourceWorkspace,
    val resource: Resource,
) : HiddenActionResourceNode(context, workspace, resource) {

    abstract fun getClientResource(resource: Resource): T

    override fun addShiftClickMenuItems(
        menu: JPopupMenu,
        paths: Array<TreePath>,
        selection: List<AbstractNavTreeNode>,
        modifiers: Int,
    ) {

        menu.addSeparator()

        menu
            .item("Copy Source") {
                val source =
                    PeriscopeDesignerContext.instance.clientResourceManager
                        .fromResource(resource)!!
                        .fileContents
                        .source
                Toolkit.getDefaultToolkit()
                    .systemClipboard
                    .setContents(StringSelection(source), null)
            }
            .apply { icon = PeriscopeIcons.tsx }

        menu
            .item("Paste Source") {
                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                val contents = clipboard.getContents(null)

                val text =
                    if (contents?.isDataFlavorSupported(DataFlavor.stringFlavor) == true)
                        contents.getTransferData(DataFlavor.stringFlavor) as String
                    else null

                if (text == null) return@item

                context.project?.createOrModify(resource.resourcePath) { builder ->
                    val sourceKey =
                        PeriscopeDesignerContext.instance.clientResourceManager
                            .fromResource(resource)!!
                            .fileLocations
                            .source
                    builder.putData(sourceKey, text.toByteArray())
                }
            }
            .apply { icon = PeriscopeIcons.tsx }
    }
}
