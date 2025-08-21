package com.mussonindustrial.ignition.embr.charts

import com.microsoft.playwright.options.LoadState
import com.mussonindustrial.ignition.embr.e2e.testing.ignition.IgnitionProject
import com.mussonindustrial.ignition.embr.e2e.testing.playwright.assertMatchesScreenshot
import com.mussonindustrial.ignition.embr.e2e.testing.playwright.usePlaywrightContainer
import io.kotest.core.spec.style.FunSpec

class GatewayTest :
    FunSpec({

        //        val gatewayUrl = System.getProperty("ignition.gatewayUrl")
        val gatewayUrl = "http://ignition:8088"
        val project = IgnitionProject(gatewayUrl, "test")

        val websocketUrl = System.getProperty("playwright.websocketUrl")
        val pw = usePlaywrightContainer(websocketUrl)

        val chartJsDefaultSchemas =
            listOf(
                "Bar",
                "BoxPlot",
                "Bubble",
                "Doughnut",
                "Line",
                "Pie",
                "PolarArea",
                "Radar",
                "Violin",
            )
        chartJsDefaultSchemas.forEach {
            test("Chart.js Default Schema - $it") {
                pw.page.apply {
                    val path = "charts/chartjs/default-schemas/$it"

                    navigate(project.perspective.pageUrl(path))
                    waitForLoadState(LoadState.NETWORKIDLE)
                    waitForTimeout(500.0)
                    assertMatchesScreenshot(path)
                }
            }
        }
    })
