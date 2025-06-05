package com.mussonindustrial.ignition.embr.charts.modules

import com.inductiveautomation.ignition.common.util.LogUtil
import com.inductiveautomation.ignition.gateway.model.GatewayModule
import com.inductiveautomation.ignition.gateway.model.ModuleObserver
import com.mussonindustrial.ignition.embr.charts.ChartsGatewayContext
import com.mussonindustrial.ignition.embr.charts.Meta

object KyvisLabsApexCharts {
    const val ID = "com.kyvislabs.apexcharts"

    class Observer(val context: ChartsGatewayContext) : ModuleObserver {

        private val logger = LogUtil.getModuleLogger(Meta.MODULE_ID, "KyvisLabsApexChartsObserver")

        init {
            this.context.ifModule(ID) { warnConflicts() }
        }

        private fun warnConflicts() {
            logger.warn(
                "Conflicts exist between Embr Charts and Kyvis Labs Apex Charts. The Kyvis Labs Apex Charts module ($ID) should be uninstalled."
            )
        }

        override fun moduleAdded(module: GatewayModule) {
            when (module.info.id) {
                ID -> warnConflicts()
            }
        }

        override fun moduleLoaded(module: GatewayModule) {
            when (module.info.id) {
                ID -> warnConflicts()
            }
        }

        override fun moduleStarted(module: GatewayModule) {
            when (module.info.id) {
                ID -> warnConflicts()
            }
        }
    }
}
