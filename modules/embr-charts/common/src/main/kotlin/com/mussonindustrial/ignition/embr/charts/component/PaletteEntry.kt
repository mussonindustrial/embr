package com.mussonindustrial.ignition.embr.charts.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl.ComponentBuilder
import com.mussonindustrial.ignition.embr.charts.Components
import java.awt.image.BufferedImage
import java.io.InputStreamReader
import javax.imageio.ImageIO

data class PaletteEntry(
    val variantId: String,
    val label: String,
    val tooltip: String,
    val thumbnail: BufferedImage,
    val props: JsonObject?
)

fun ComponentBuilder.addPaletteEntry(entry: PaletteEntry): ComponentBuilder {
    this.addPaletteEntry(entry.variantId, entry.label, entry.tooltip, entry.thumbnail, entry.props)
    return this
}

fun getIcon(path: String): BufferedImage {
    return ImageIO.read(Components::class.java.getResource(path))
}

fun getJsonProps(path: String): JsonObject {
    return JsonParser.parseReader(
        Components::class.java.getResourceAsStream(path)
        ?.let { InputStreamReader(it) }).asJsonObject
}