package at.searles.fractimageview.plugins

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.util.TypedValue
import android.view.MotionEvent
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractimageview.R

class IconIfFlippedPlugin(context: Context): Plugin {

    override var isEnabled: Boolean = true

    private val icon = context.resources.getDrawable(R.drawable.flipped_icon, null)
    private val iconSize = dpToPx(iconSizeDp, context.resources)

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        val vw = source.width.toFloat()
        val vh = source.height.toFloat()

        if(source.isBitmapFlipped) {
            icon.setBounds(
                (vw - iconSize).toInt(), (vh - iconSize).toInt(),
                vw.toInt(), vh.toInt())

            icon.draw(canvas)
        }
    }

    override fun onTouchEvent(source: PluginScalableImageView, event: MotionEvent): Boolean = false

    private fun dpToPx(dip: Float, resources: Resources): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            resources.displayMetrics
        )
    }

    companion object {
        private const val iconSizeDp = 24f
    }
}