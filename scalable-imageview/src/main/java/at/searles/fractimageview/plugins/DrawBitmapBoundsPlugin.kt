package at.searles.fractimageview.plugins

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.PluginScalableImageView

class DrawBitmapBoundsPlugin: Plugin {

    override var isEnabled: Boolean = true

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        val vw = source.width.toFloat()
        val vh = source.height.toFloat()

        val cx = vw / 2f
        val cy = vh / 2f
        val sbw = source.scaledBitmapWidth
        val sbh = source.scaledBitmapHeight

        // draw in total 4 transparent rectangles to indicate the drawing area
        canvas.drawRect(-1f, -1f, vw, cy - sbh / 2f,
            boundsPaint
        ) // top
        canvas.drawRect(-1f, -1f, cx - sbw / 2f, vh,
            boundsPaint
        ) // left
        canvas.drawRect(-1f, cy + sbh / 2f, vw, vh,
            boundsPaint
        ) // bottom
        canvas.drawRect(cx + sbw / 2f, -1f, vw, vh,
            boundsPaint
        ) // right
    }

    override fun onTouchEvent(source: PluginScalableImageView, event: MotionEvent): Boolean = false

    companion object {
        val boundsPaint = Paint().apply {
            color = -0x80000000 // semi-transparent black
            style = Paint.Style.FILL_AND_STROKE
        }
    }
}