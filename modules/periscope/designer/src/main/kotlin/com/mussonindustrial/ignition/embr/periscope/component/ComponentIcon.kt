package com.mussonindustrial.ignition.embr.periscope.component

import com.mussonindustrial.ignition.embr.periscope.icons.PeriscopeIcons
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.swing.Icon

class ComponentIcon : Icon {

    companion object {
        private const val SIZE = 16
    }

    private var name: String? = null
    private var image: BufferedImage? = null
    private var loaded = false

    val missing: Boolean
        get() = name != null && loaded && image == null

    fun show(iconName: String?) {
        if (name == iconName) {
            return
        }
        name = iconName
        image = null
        loaded = false

        if (iconName == null) {
            return
        }
        ComponentIcons.loadImage(iconName) { loadedImage ->
            if (name == iconName) {
                image = loadedImage
                loaded = true
                ComponentIcons.onImageLoaded()
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
        val loadedImage = image
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
