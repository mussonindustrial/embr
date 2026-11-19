package com.mussonindustrial.ignition.embr.periscope.component

import com.mussonindustrial.embr.common.logging.getLoggerEx
import com.mussonindustrial.embr.common.reflect.getPrivateMethod
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import javax.swing.Icon

internal object ComponentIcons {
    private const val ICON_PICKER =
        "com.inductiveautomation.perspective.designer.workspace.propertyeditor.IconPicker"

    private val logger = this.getLoggerEx()
    private val icons = ConcurrentHashMap<String, ComponentIcon>()
    private val libraryIcons = ConcurrentHashMap<String, ComponentIcon>()
    private val images = ConcurrentHashMap<String, CompletableFuture<BufferedImage?>>()

    @Volatile var onImageLoaded: () -> Unit = {}

    fun get(componentId: String): ComponentIcon {
        return icons.computeIfAbsent(componentId) { ComponentIcon() }
    }

    fun library(path: String): ComponentIcon {
        return libraryIcons.computeIfAbsent(path) { ComponentIcon(PeriscopeIcons.componentLibrary) }
    }

    fun setLibraryIcons(iconNames: Map<String, String?>) {
        for (path in libraryIcons.keys + iconNames.keys) {
            library(path).iconName = iconNames[path]
        }
    }

    fun loadImage(iconName: String): CompletableFuture<BufferedImage?> {
        return images.computeIfAbsent(iconName) {
            val parts = iconName.split('/', limit = 2)
            if (parts.size != 2) {
                return@computeIfAbsent CompletableFuture.completedFuture(null)
            }

            val (library, name) = parts
            loadSprites(library).thenApply { sprites ->
                val sprite = sprites[name]
                if (sprite == null) null else imageOf(sprite)
            }
        }
    }

    private fun loadSprites(library: String): CompletableFuture<Map<String, Any>> {
        return try {
            val method =
                Class.forName(ICON_PICKER).getPrivateMethod("getSprites", String::class.java)
            @Suppress("UNCHECKED_CAST")
            method.invoke(null, library) as CompletableFuture<Map<String, Any>>
        } catch (e: Throwable) {
            logger.debug("Could not load icon library '$library'.", e)
            CompletableFuture.completedFuture(emptyMap())
        }
    }

    private fun imageOf(sprite: Any): BufferedImage? {
        return try {
            sprite.getPrivateProperty("image") as BufferedImage
        } catch (e: Throwable) {
            null
        }
    }

    class ComponentIcon(private val fallback: Icon = PeriscopeIcons.tsx) : Icon {

        @Volatile private var image: BufferedImage? = null
        @Volatile private var loaded = false

        val missing: Boolean
            get() = iconName != null && loaded && image == null

        @Volatile
        var iconName: String? = null
            set(value) {
                if (field == value) return
                field = value
                image = null
                loaded = false

                if (value == null) return
                ComponentIcons.loadImage(value).thenAccept { loadedImage ->
                    if (field == value) {
                        image = loadedImage
                        loaded = true
                        ComponentIcons.onImageLoaded()
                    }
                }
            }

        override fun getIconWidth() = SIZE

        override fun getIconHeight() = SIZE

        override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
            val image = image
            if (image == null) {
                fallback.paintIcon(c, g, x, y)
                return
            }

            val graphics = g.create() as Graphics2D
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC,
            )
            graphics.drawImage(image, x, y, SIZE, SIZE, null)
            graphics.dispose()
        }

        companion object {
            private const val SIZE = 16
        }
    }
}
