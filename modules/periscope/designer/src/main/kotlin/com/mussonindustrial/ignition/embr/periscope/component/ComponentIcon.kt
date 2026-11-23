package com.mussonindustrial.ignition.embr.periscope.component

import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Icon

class ComponentIcon : Icon {

    companion object {
        private const val SIZE = 16
    }

    private val name = AtomicReference<String?>()
    private val image = AtomicReference<BufferedImage?>()
    private val loaded = AtomicBoolean(false)

    val missing: Boolean
        get() = name.get() != null && loaded.get() && image.get() == null

    fun show(iconName: String?) {
        if (name.getAndSet(iconName) == iconName) {
            return
        }
        image.set(null)
        loaded.set(false)

        if (iconName == null) {
            return
        }
        ComponentIcons.loadImage(iconName).thenAccept { loadedImage ->
            if (name.get() == iconName) {
                image.set(loadedImage)
                loaded.set(true)
                ComponentIcons.onImageLoaded.get().invoke()
            }
        }
    }

    override fun getIconWidth(): Int {
        return SIZE
    }

    override fun getIconHeight(): Int {
        return SIZE
    }

    override fun paintIcon(c: Component?, g: Graphics, x: Int, y: Int) {
        val loadedImage = image.get()
        if (loadedImage == null) {
            PeriscopeIcons.component.paintIcon(c, g, x, y)
            return
        }

        val graphics = g.create() as Graphics2D
        graphics.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC,
        )
        graphics.drawImage(loadedImage, x, y, SIZE, SIZE, null)
        graphics.dispose()
    }
}
