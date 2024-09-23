package com.mussonindustrial.embr.perspective.common.component

import com.inductiveautomation.ignition.common.gson.JsonObject
import com.inductiveautomation.ignition.common.gson.JsonParser
import com.inductiveautomation.perspective.common.api.ComponentDescriptorImpl.ComponentBuilder
import java.awt.image.BufferedImage
import java.io.InputStreamReader
import javax.imageio.ImageIO

data class PaletteEntry(
    val variantId: String,
    val label: String,
    val tooltip: String,
    val thumbnail: BufferedImage?,
    val props: JsonObject?,
) {
    constructor(
        source: Class<*>,
        variantId: String,
        label: String,
        tooltip: String
    ) : this(
        variantId,
        label,
        tooltip,
        getImage(source, "/images/components/thumbnails/$variantId.png"),
        getJsonProps(source, "/variants/$variantId.props.json"),
    )
}

fun ComponentBuilder.addPaletteEntry(entry: PaletteEntry): ComponentBuilder {
    this.addPaletteEntry(entry.variantId, entry.label, entry.tooltip, entry.thumbnail, entry.props)
    return this
}

fun getImage(
    source: Class<*>,
    path: String,
): BufferedImage? {
    val resource = source.getResource(path)
    resource?.let {
        return ImageIO.read(it)
    }
    return null
}

fun getJsonProps(
    source: Class<*>,
    path: String,
): JsonObject {
    return JsonParser.parseReader(
            source.getResourceAsStream(path)?.let { InputStreamReader(it) },
        )
        .asJsonObject
}
