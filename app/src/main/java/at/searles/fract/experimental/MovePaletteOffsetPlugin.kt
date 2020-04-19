package at.searles.fract.experimental

import android.graphics.Canvas
import android.graphics.PointF
import android.view.MotionEvent
import at.searles.commons.color.Palette
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.changes.BitmapPropertiesChange
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.ScalableImageView
import kotlin.math.min

class MovePaletteOffsetPlugin(private val bitmapModel: FractBitmapModel): Plugin {

    override var isEnabled = false

    private var isActive = false
    private lateinit var startPoint: PointF
    private lateinit var paletteLabel: String
    private lateinit var palette: Palette

    override fun onDraw(source: ScalableImageView, canvas: Canvas) {
        // TODO show label of palette
    }

    override fun onTouchEvent(source: ScalableImageView, event: MotionEvent): Boolean {
        if(!isActive) {
            if(event.action == MotionEvent.ACTION_DOWN) {
                isActive = true
                startPoint = PointF(event.x, event.y)

                // TODO: Pick palette based on background.
                paletteLabel = bitmapModel.properties.paletteLabels.first()
                palette = bitmapModel.properties.getPalette(paletteLabel)
            } else {
                return false
            }
        }

        if(event.action == MotionEvent.ACTION_UP) {
            isActive = false
            return true
        }

        val size = min(source.width, source.height) * 0.75f

        val deltaX = (startPoint.x - event.x) / size
        val deltaY = (startPoint.y - event.y) / size

        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val newPalette = palette.let {
                    Palette(it.width, it.height, it.offsetX + deltaX, it.offsetY + deltaY, it.colorPoints)
                }

                val customPalettes = properties.customPalettes.toMutableMap().apply {
                    this[paletteLabel] = newPalette
                }

                return properties.createWithNewBitmapProperties(customPalettes, null)
            }
        }

        bitmapModel.applyBitmapPropertiesChange(change)
        source.invalidate()
        return true
    }
}