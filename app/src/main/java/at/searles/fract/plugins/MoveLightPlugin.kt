package at.searles.fract.plugins

import android.graphics.Canvas
import android.graphics.PointF
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.ShaderProperties
import at.searles.fractbitmapmodel.changes.BitmapPropertiesChange
import at.searles.fractimageview.PluginScalableImageView
import kotlin.math.*

class MoveLightPlugin(private val bitmapModel: () -> FractBitmapModel): MotionPlugin() {

    override var isEnabled = false

    /*
        x = (sin(polarAngle) * cos(azimuthAngle)).toFloat(),
        y = -(sin(polarAngle) * sin(azimuthAngle)).toFloat(),
        z = cos(polarAngle).toFloat()
     */

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
    }

    override fun activatePlugin(source: PluginScalableImageView) {
        bitmapModel().startAnimation(720) // TODO
    }

    override fun deactivatePlugin() {
        bitmapModel().stopAnimation()
    }

    override fun movePointer(source: PluginScalableImageView) {
        val newShaderProperties = getShaderPropertiesForPoint(source, PointF(currentTouchX, currentTouchY))

        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                return properties.createWithNewBitmapProperties(null, newShaderProperties)
            }
        }

        bitmapModel().applyBitmapPropertiesChange(change)
    }

    private fun getShaderPropertiesForPoint(source: PluginScalableImageView, pt: PointF): ShaderProperties {
        val pt0 = source.norm(pt)

        val azimuth = -atan2(pt0.y, pt0.x).toDouble()
        val polar = asin(max(0f, min(1f, hypot(pt0.x, pt0.y)))).toDouble()

        return bitmapModel().properties.shaderProperties.createWithNewLightVector(polar, azimuth)
    }
}