package com.mussonindustrial.embr.designer.gui

import com.inductiveautomation.ignition.client.util.BrowserLauncher
import com.inductiveautomation.ignition.common.licensing.LicenseMode
import com.mussonindustrial.embr.common.Embr
import com.mussonindustrial.embr.designer.EmbrDesignerContext
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.SwingUtilities

class EmbrStatusBarButton(private val context: EmbrDesignerContext) : JLabel() {

    val colorRed = "#ff5c5c"
    val colorGreen = "#5cff7c"

    init {
        icon = EmbrDesignerIcons.emblem
        text = if (context.unlicensedEmbrModules.isNotEmpty()) "Embr (UNLICENSED)" else ""
        toolTipText = buildTooltipHtml()

        addMouseListener(
            object : MouseAdapter() {
                override fun mousePressed(e: MouseEvent) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        BrowserLauncher.openURL(Embr.DOCUMENTATION_URL)
                    }
                }
            }
        )
    }

    private fun buildTooltipHtml(): String {
        val moduleList =
            context.embrModules.joinToString("<br>") { module ->
                val licenseMode = context.getLicenseState(module.id).licenseMode
                val isLicensed =
                    licenseMode == LicenseMode.Activated || licenseMode == LicenseMode.Free
                if (isLicensed) {
                    "<font color='$colorGreen'>${module.name}</font>"
                } else {
                    "<font color='$colorRed'>${module.name} <b>(<u>UNLICENSED</u>)</b></font>"
                }
            }

        return """
            <html>
                <b>Embr by Musson Industrial</b>
                <p>This gateway is utilizing Embr, a collection of open-source modules by Musson Industrial.</p>
                $moduleList
                <br>
                You can view the documentation by clicking here.<br>
                Need support? We can help!
            </html>
        """
            .trimIndent()
    }
}
