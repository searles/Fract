package at.searles.fract.experimental

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import at.searles.commons.color.Palette
import at.searles.fract.FractSettings
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.changes.BitmapPropertiesChange
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.ScalableImageView
import kotlin.math.floor
import kotlin.math.min

class MovePaletteOffsetPlugin(private val settings: () -> FractSettings, private val bitmapModel: () -> FractBitmapModel): Plugin {

    override var isEnabled = false

    private var isActive = false
    private lateinit var startPoint: PointF

    private lateinit var palettes: Map<String, Palette>

    override fun onDraw(source: ScalableImageView, canvas: Canvas) {
        // Do nothing.
    }

    override fun onTouchEvent(source: ScalableImageView, event: MotionEvent): Boolean {
        if(!isActive) {
            if(event.action == MotionEvent.ACTION_DOWN) {
                isActive = true
                startPoint = PointF(event.x, event.y)
                palettes = bitmapModel().properties.paletteLabels.filter {
                    !settings().excludeFromPaletteEdit.contains(it)
                }.map { it to bitmapModel().properties.getPalette(it) }.toMap()
            } else {
                return false
            }
        }

        if(event.action == MotionEvent.ACTION_UP) {
            isActive = false
            return true
        }

        val size = min(source.width, source.height) * 0.75f

        val deltaX = (event.x - startPoint.x) / size
        val deltaY = (event.y - startPoint.y) / size

        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val customPalettes = properties.customPalettes.toMutableMap()

                palettes.forEach {
                    (label, palette) -> customPalettes[label] = createShiftedPalette(palette, deltaX, deltaY)
                }

                return properties.createWithNewBitmapProperties(customPalettes, null)
            }
        }

        bitmapModel().applyBitmapPropertiesChange(change)
        source.invalidate()
        return true
    }

    private fun createShiftedPalette(palette: Palette, deltaX: Float, deltaY: Float): Palette {
        val newOffsetX = (palette.offsetX + deltaX).let { it - floor(it) }
        val newOffsetY = (palette.offsetY + deltaY).let { it - floor(it) }

        return Palette(palette.width, palette.height, newOffsetX, newOffsetY, palette.colorPoints)
    }
}