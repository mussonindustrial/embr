package com.mussonindustrial.ignition.embr.charts.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl.ComponentBuilder
import com.mussonindustrial.ignition.embr.charts.Meta
import java.awt.image.BufferedImage
import java.io.InputStreamReader
import javax.imageio.ImageIO

data class PaletteEntry(
    val variantId: String,
    val label: String,
    val tooltip: String,
    val thumbnail: BufferedImage?,
    val props: JsonObject?
) {
    constructor(
        variantId: String,
        label: String,
        tooltip: String
    ) : this(
        variantId,
        label,
        tooltip,
        getImage("/images/components/thumbnails/${variantId}.png"),
        getJsonProps("/variants/${variantId}.props.json")
    )
}

fun ComponentBuilder.addPaletteEntry(entry: PaletteEntry): ComponentBuilder {
    this.addPaletteEntry(entry.variantId, entry.label, entry.tooltip, entry.thumbnail, entry.props)
    return this
}

fun getImage(path: String): BufferedImage? {
    val resource = Meta::class.java.getResource(path)
    resource?.let {
        return ImageIO.read(it)
    }
    return null
}

fun getJsonProps(path: String): JsonObject {
    return JsonParser.parseReader(
            Meta::class.java.getResourceAsStream(path)?.let { InputStreamReader(it) }
        )
        .asJsonObject
}
