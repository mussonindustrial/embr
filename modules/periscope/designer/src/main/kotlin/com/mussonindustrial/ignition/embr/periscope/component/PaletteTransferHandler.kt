package com.mussonindustrial.ignition.embr.periscope.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.perspective.designer.model.DropObject
import com.mussonindustrial.ignition.embr.periscope.resources.language.ComponentVariant
import java.awt.datatransfer.Transferable
import javax.swing.JComponent
import javax.swing.TransferHandler

class PaletteTransferHandler(
    private val tree: PaletteTree,
    private val onDropped: (ComponentInfo) -> Unit,
) : TransferHandler() {

    companion object {
        private const val DROP_TYPE = "embr-periscope-project-component"
    }

    override fun getSourceActions(c: JComponent): Int {
        return COPY
    }

    override fun createTransferable(c: JComponent): Transferable? {
        return when (val value = tree.selectedValue) {
            is ComponentInfo -> transferableOf(value, null)
            is PaletteVariant -> transferableOf(value.info, value.variant)
            else -> null
        }
    }

    override fun exportDone(source: JComponent, data: Transferable?, action: Int) {
        if (action == NONE) {
            return
        }
        val info = tree.selectedComponent ?: return
        onDropped(info)
    }

    private fun transferableOf(
        info: ComponentInfo,
        variant: ComponentVariant?,
    ): Transferable {
        val size = variant?.defaultSize ?: info.definition.defaultSize
        val payload =
            JsonObject().apply {
                addProperty("type", ProjectComponents.getType(info.id))
                val name = info.name + variant?.name.orEmpty()
                addProperty("name", name.filter(Char::isLetterOrDigit))
                add("props", variant?.props ?: info.definition.defaults)
                if (size != null) {
                    addProperty("width", size.width)
                    addProperty("height", size.height)
                }
            }
        return DropObject.createTransferable(DROP_TYPE, payload)
    }
}
