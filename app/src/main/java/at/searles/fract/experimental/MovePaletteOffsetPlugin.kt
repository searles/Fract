package at.searles.fract.experimental

import android.graphics.Canvas
import at.searles.commons.color.Palette
import at.searles.fract.FractSettings
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.changes.BitmapPropertiesChange
import at.searles.fractimageview.ScalableImageView
import kotlin.math.floor
import kotlin.math.min

class MovePaletteOffsetPlugin(private val settings: () -> FractSettings, private val bitmapModel: () -> FractBitmapModel): MotionPlugin() {

    override var isEnabled = false

    private lateinit var palettes: Map<String, Palette>

    private var totalDeltaX = 0f
    private var totalDeltaY = 0f

    override fun onDraw(source: ScalableImageView, canvas: Canvas) {
        // Do nothing.
    }

    override fun activatePlugin() {
        palettes = bitmapModel().properties.paletteLabels.filter {
            !settings().excludeFromPaletteMode.contains(it)
        }.map { it to bitmapModel().properties.getPalette(it) }.toMap()

        bitmapModel().startAnimation(720) // TODO

        totalDeltaX = 0f
        totalDeltaY = 0f
    }

    override fun deactivatePlugin() {
        bitmapModel().stopAnimation()
    }

    override fun movePointer(source: ScalableImageView) {
        val size = min(source.width, source.height) * 0.75f

        require(size > 0)

        totalDeltaX += this.deltaX / size
        totalDeltaY += this.deltaY / size

        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val customPalettes = properties.customPalettes.toMutableMap()

                palettes.forEach {
                        (label, palette) -> customPalettes[label] = createShiftedPalette(palette)
                }

                return properties.createWithNewBitmapProperties(customPalettes, null)
            }
        }

        bitmapModel().applyBitmapPropertiesChange(change)
    }

    private fun createShiftedPalette(palette: Palette): Palette {
        val newOffsetX = (palette.offsetX + totalDeltaX).let { it - floor(it) }
        val newOffsetY = (palette.offsetY + totalDeltaY).let { it - floor(it) }

        return Palette(palette.width, palette.height, newOffsetX, newOffsetY, palette.colorPoints)
    }
}