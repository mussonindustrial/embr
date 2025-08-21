package com.mussonindustrial.ignition.embr.charts

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import com.github.romankh3.image.comparison.model.ImageComparisonState
import com.microsoft.playwright.Page
import com.microsoft.playwright.options.LoadState
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

class GatewayTest :
    FunSpec({
        install(Containers.extension)
        val pw = extension(PlaywrightExtension())

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
                pw.page.assertMatchesScreenshot("test", "charts/chartjs/default-schemas/$it")
            }
        }
    })

fun perspectivePage(project: String, path: String): String {
    return "${Containers.gateway.gatewayUrl}/data/perspective/client/$project/$path"
}

fun baselinePath(testName: String): Path = Paths.get("src/test/resources/screenshots/$testName.png")

fun currentPath(testName: String): Path =
    Paths.get("build/test-results/screenshots/${testName}-current.png")

fun diffPath(testName: String): Path =
    Paths.get("build/test-results/screenshots/${testName}-diff.png")

/** Navigate to a Perspective page and assert screenshot against baseline. */
fun Page.assertMatchesScreenshot(project: String, path: String) {
    val url = perspectivePage(project, path)
    val baseline = baselinePath(path)
    val current = currentPath(path)

    navigate(url)
    waitForLoadState(LoadState.NETWORKIDLE)
    waitForTimeout(500.0)

    screenshot(Page.ScreenshotOptions().setPath(current))

    if (System.getProperty("updateScreenshots") == "true") {
        // overwrite baseline
        Files.createDirectories(baseline.parent)
        current.toFile().copyTo(baseline.toFile(), overwrite = true)
        println("Baseline updated: $baseline")
        return
    }

    require(baseline.toFile().exists()) {
        "Baseline not found: $baseline. Run with -DupdateScreenshots=true to create it."
    }

    val expected = ImageComparisonUtil.readImageFromResources(baseline.toString())
    val actual = ImageComparisonUtil.readImageFromResources(current.toString())
    val result = ImageComparison(expected, actual).compareImages()

    if (result.imageComparisonState == ImageComparisonState.MISMATCH) {
        Files.createDirectories(diffPath(path).parent)
        ImageIO.write(result.result, "png", diffPath(path).toFile())
        error("Visual regression in $path! See ${diffPath(path)} for diff.")
    }

    println("Screenshot matches baseline: $baseline")
}
