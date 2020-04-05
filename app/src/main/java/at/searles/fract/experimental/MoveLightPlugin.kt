package at.searles.fract.experimental

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.TypedValue
import android.view.MotionEvent
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.ShaderProperties
import at.searles.fractbitmapmodel.changes.BitmapPropertiesChange
import at.searles.fractimageview.ScalableBitmapViewUtils
import at.searles.fractimageview.ScalableImageView
import kotlin.math.*

class MoveLightPlugin(private val context: Context, private val bitmapModel: FractBitmapModel): ScalableImageView.Plugin {

    /**
     *
    x = (sin(polarAngle) * cos(azimuthAngle)).toFloat(),
    y = -(sin(polarAngle) * sin(azimuthAngle)).toFloat(),
    z = cos(polarAngle).toFloat()
     */

    private var isDragging = false

    private val paint: Paint = Paint().apply {
        this.strokeWidth = dpToPx(strokeWidthDp, context.resources)
        this.style = Paint.Style.STROKE
    }

    override fun onDraw(source: ScalableImageView, canvas: Canvas) {
        val pt = getLightPoint(source)

        val r = dpToPx(touchDistanceDp, context.resources)

        val transparency = if(isDragging) 0x80000000.toInt() else 0x40000000

        canvas.drawLine(pt.x - r / sqrt(2f), pt.y - r / sqrt(2f), pt.x + r / sqrt(2f), pt.y + r / sqrt(2f), paint.apply { color = 0xffffff or transparency; strokeWidth *= 2f })
        canvas.drawLine(pt.x - r / sqrt(2f), pt.y - r / sqrt(2f), pt.x + r / sqrt(2f), pt.y + r / sqrt(2f), paint.apply { color = 0x000000 or transparency; strokeWidth /= 2f })
        canvas.drawLine(pt.x + r / sqrt(2f), pt.y - r / sqrt(2f), pt.x - r / sqrt(2f), pt.y + r / sqrt(2f), paint.apply { color = 0xffffff or transparency; strokeWidth *= 2f })
        canvas.drawLine(pt.x + r / sqrt(2f), pt.y - r / sqrt(2f), pt.x - r / sqrt(2f), pt.y + r / sqrt(2f), paint.apply { color = 0x000000 or transparency; strokeWidth /= 2f })

        canvas.drawCircle(pt.x, pt.y, r, paint.apply { color = 0xffffff or transparency })
        canvas.drawCircle(pt.x, pt.y, r - paint.strokeWidth, paint.apply { color = transparency })
    }

    override fun onTouchEvent(source: ScalableImageView, event: MotionEvent): Boolean {
        if(!isDragging) {
            if(event.action == MotionEvent.ACTION_DOWN) {
                val pt = getLightPoint(source)

                val r = dpToPx(touchDistanceDp, context.resources)

                if(hypot(event.x - pt.x, event.y - pt.y) >= r) {
                    return false
                }

                isDragging = true
            } else {
                return false
            }
        }

        if(event.action == MotionEvent.ACTION_UP) {
            isDragging = false
            return true
        }

        val newShaderProperties = getShaderPropertiesForPoint(source, PointF(event.x, event.y))

        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                return properties.createWithNewBitmapProperties(null, newShaderProperties)
            }
        }

        bitmapModel.applyBitmapPropertiesChange(change)
        source.invalidate()
        return true
    }

    private fun getLightPoint(source: ScalableImageView): PointF {
        // First get normalized point.
        val lightVector = bitmapModel.properties.shaderProperties.lightVector

        val lightPt = PointF(lightVector.x, lightVector.y)

        return ScalableBitmapViewUtils.invNorm(lightPt,
            bitmapModel.width.toFloat(), bitmapModel.height.toFloat(),
            source.width.toFloat(), source.height.toFloat()
        )
    }

    private fun getShaderPropertiesForPoint(source: ScalableImageView, pt: PointF): ShaderProperties {
        val pt0 = ScalableBitmapViewUtils.norm(pt,
            bitmapModel.width.toFloat(), bitmapModel.height.toFloat(),
            source.width.toFloat(), source.height.toFloat()
        )

        val azimuth = -atan2(pt0.y, pt0.x).toDouble()
        val polar = asin(max(0f, min(1f, hypot(pt0.x, pt0.y)))).toDouble()

        return bitmapModel.properties.shaderProperties.createWithNewLightVector(polar, azimuth)
    }

    private fun dpToPx(dip: Float, resources: Resources): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            resources.displayMetrics
        )
    }

    companion object {
        private const val strokeWidthDp = 2f
        private const val touchDistanceDp = 18f
    }
}