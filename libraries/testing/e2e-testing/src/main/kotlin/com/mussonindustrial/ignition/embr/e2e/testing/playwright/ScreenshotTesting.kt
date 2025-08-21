package com.mussonindustrial.ignition.embr.e2e.testing.playwright

import com.github.romankh3.image.comparison.ImageComparison
import com.github.romankh3.image.comparison.ImageComparisonUtil
import com.github.romankh3.image.comparison.model.ImageComparisonState
import com.microsoft.playwright.Page
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

fun baselinePath(testName: String): Path = Paths.get("src/test/resources/screenshots/$testName.png")

fun currentPath(testName: String): Path =
    Paths.get("build/test-results/screenshots/${testName}-current.png")

fun diffPath(testName: String): Path =
    Paths.get("build/test-results/screenshots/${testName}-diff.png")

fun Page.assertMatchesScreenshot(testName: String) {
    val baseline = baselinePath(testName)
    val current = currentPath(testName)

    screenshot(Page.ScreenshotOptions().setPath(current))

    if (System.getProperty("updateScreenshots") == "true") {
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
        Files.createDirectories(diffPath(testName).parent)
        ImageIO.write(result.result, "png", diffPath(testName).toFile())
        error("Visual regression in $testName! See ${diffPath(testName)} for diff.")
    }

    println("Screenshot matches baseline: $baseline")
}
