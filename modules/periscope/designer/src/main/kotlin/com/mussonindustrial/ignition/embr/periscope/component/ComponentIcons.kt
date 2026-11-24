package com.mussonindustrial.ignition.embr.periscope.component

import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.common.reflect.getPrivateMethod
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import javax.swing.SwingUtilities.invokeLater

/** Component icons, for use on the event dispatch thread only. */
object ComponentIcons {
    private const val ICON_PICKER =
        "com.inductiveautomation.perspective.designer.workspace.propertyeditor.IconPicker"

    private val logger = this.getLoggerEx()
    private val iconsByComponent = mutableMapOf<String, ComponentIcon>()
    private val imagesByName = mutableMapOf<String, CompletableFuture<BufferedImage?>>()

    var onImageLoaded: () -> Unit = {}

    fun get(componentId: String): ComponentIcon {
        return iconsByComponent.getOrPut(componentId) { ComponentIcon() }
    }

    /** Calls back on the event dispatch thread, with no image when the icon cannot be found. */
    fun loadImage(iconName: String, onLoaded: (BufferedImage?) -> Unit) {
        imagesByName
            .getOrPut(iconName) { findImage(iconName) }
            .thenAccept { image -> invokeLater { onLoaded(image) } }
    }

    private fun findImage(iconName: String): CompletableFuture<BufferedImage?> {
        val parts = iconName.split('/', limit = 2)
        if (parts.size != 2) {
            return CompletableFuture.completedFuture(null)
        }

        val (library, name) = parts
        return loadSprites(library).thenApply { sprites -> sprites[name]?.let(::imageOf) }
    }

    private fun loadSprites(library: String): CompletableFuture<Map<String, Any>> {
        return try {
            val method =
                Class.forName(ICON_PICKER).getPrivateMethod("getSprites", String::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, library) as CompletableFuture<Map<String, Any>>
        } catch (e: Throwable) {
            logger.debug("Failed to load icon library '$library'.", e)
            CompletableFuture.completedFuture(emptyMap())
        }
    }

    private fun imageOf(sprite: Any): BufferedImage? {
        return try {
            sprite.getPrivateProperty("image") as BufferedImage
        } catch (e: Exception) {
            logger.debug("Failed to read an icon image.", e)
            null
        }
    }
}
