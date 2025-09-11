package com.mussonindustrial.ignition.embr.charts

import com.microsoft.playwright.options.LoadState
import com.mussonindustrial.ignition.embr.e2e.testing.ignition.IgnitionProject
import com.mussonindustrial.ignition.embr.e2e.testing.playwright.assertMatchesScreenshot
import com.mussonindustrial.ignition.embr.e2e.testing.playwright.usePlaywrightContainer
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class GatewayTest :
    FunSpec({

        val gatewayUrl = "http://ignition:8088"
        val project = IgnitionProject(gatewayUrl, "test")

        val websocketUrl = System.getProperty("playwright.websocketUrl")
        val pw = usePlaywrightContainer(websocketUrl)

        fun testDefaultSchemas(component: String, schemas: List<String>) {
            schemas.forEach { schema ->
                test("$component default-schema - $schema") {
                    coroutineScope {
                        launch {
                            val context = pw.browser.newContext()
                            val page = context.newPage()
                            val path = "charts/$component/default-schema/$schema"

                            page.navigate(project.perspective.pageUrl(path))
                            page.waitForLoadState(LoadState.NETWORKIDLE)
                            page.assertMatchesScreenshot(path)
                        }
                    }
                }
            }
        }

        testDefaultSchemas( "apexcharts", listOf(
            "Area",
            "Bar",
            "BoxPlot",
            "Bubble",
            "Candlestick",
            "Column",
            "Funnel",
            "HeatMap",
            "Line",
            "Mixed",
            "Pie",
            "PolarArea",
            "Radar",
            "RadialBar",
            "RangeArea",
            "Scatter",
            "Slope",
            "TreeMap",
        ))

        testDefaultSchemas( "apexcharts-legacy", listOf(
            "Line",
            "Pie",
            "Radar",
            "TimeSeries",
        ))

        testDefaultSchemas( "chartjs", listOf(
            "Bar",
            "BoxPlot",
            "Bubble",
            "Doughnut",
            "Line",
            "Pie",
            "PolarArea",
            "Radar",
            "Violin",
        ))
    })