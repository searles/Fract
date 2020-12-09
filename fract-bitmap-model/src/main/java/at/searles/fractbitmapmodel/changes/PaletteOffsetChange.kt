package at.searles.fractbitmapmodel.changes

import at.searles.commons.color.Palette
import at.searles.fractbitmapmodel.FractProperties

class PaletteOffsetChange(private val label: String, private val offsetX: Float, private val offsetY: Float, override val useFastRoot: Boolean = false): BitmapPropertiesChange {
    override fun accept(properties: FractProperties): FractProperties {
        val oldPalette = properties.getPalette(label)
        val newPalette = Palette(oldPalette.width, oldPalette.height, offsetX, offsetY, oldPalette.colorPoints)

        val newPaletteMap = properties.customPalettes.toMutableMap().apply {
            put(label, newPalette)
        }

        return properties.createWithNewBitmapProperties(newPaletteMap, null)
    }
}