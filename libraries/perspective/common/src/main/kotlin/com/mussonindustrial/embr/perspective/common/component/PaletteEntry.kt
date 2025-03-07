package com.mussonindustrial.embr.perspective.common.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl.ComponentBuilder
import java.awt.image.BufferedImage
import java.io.InputStreamReader
import javax.imageio.ImageIO

data class PaletteEntry(
    val clazz: Class<*>,
    val componentId: String,
    val variantId: String,
    val label: String,
    val tooltip: String,
    val thumbnail: BufferedImage?,
    val props: JsonObject?,
) {
    constructor(
        clazz: Class<*>,
        componentId: String,
        variantId: String,
        label: String,
        tooltip: String,
    ) : this(
        clazz,
        componentId,
        variantId,
        label,
        tooltip,
        getImage(clazz, "/images/components/${componentId}/thumbnails/${variantId}.png"),
        getJsonProps(clazz, "/schemas/components/${componentId}/variants/${variantId}.props.json"),
    )
}

fun ComponentBuilder.addPaletteEntry(entry: PaletteEntry): ComponentBuilder {
    this.addPaletteEntry(entry.variantId, entry.label, entry.tooltip, entry.thumbnail, entry.props)
    return this
}

fun getImage(clazz: Class<*>, path: String): BufferedImage? {
    val resource = PaletteEntry::class.java.getResource(path)
    resource?.let {
        return ImageIO.read(it)
    }
    return null
}

fun getJsonProps(clazz: Class<*>, path: String): JsonObject {
    return JsonParser.parseReader(
            PaletteEntry::class.java.getResourceAsStream(path)?.let { InputStreamReader(it) }
        )
        .asJsonObject
}
