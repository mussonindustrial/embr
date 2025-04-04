package com.mussonindustrial.ignition.embr.periscope.navtree.model

import com.inductiveautomation.ignition.client.icons.SvgIconUtil
import com.inductiveautomation.ignition.common.BundleUtil
import com.inductiveautomation.ignition.designer.navtree.model.AbstractNavTreeNode
import com.inductiveautomation.ignition.designer.navtree.model.MutableNavTreeNode
import com.mussonindustrial.ignition.embr.periscope.Meta
import java.awt.Component
import javax.swing.Icon
import javax.swing.tree.TreePath

class WebLibraryRootNode(isModuleNode: Boolean) : MutableNavTreeNode(isModuleNode) {
    override fun showPopupMenu(
        source: Component,
        x: Int,
        y: Int,
        modifiers: Int,
        paths: Array<TreePath>,
        selection: List<AbstractNavTreeNode>,
    ) {}

    override fun getText(): String {
        return BundleUtil.i18n("periscope.web-library.noun")
    }

    override fun getSortOrder(): Int {
        return 9999999
    }

    override fun getIcon(): Icon {
        return if (this.isSelected) {
            SvgIconUtil.getIcon(Meta::class.java, "theming-selected")
        } else {
            SvgIconUtil.getIcon(Meta::class.java, "theming")
        }
    }
}
