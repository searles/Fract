package at.searles.fractimageview.demo

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.view.MotionEvent
import android.widget.TextView
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractimageview.ScalableImageView

class DemoPlugin(private val tv: TextView):
    Plugin {

    private val paint = Paint().apply {
        strokeWidth = 4f
        color = Color.BLACK
    }
    override var isEnabled = true

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        val corners = floatArrayOf(-1f, -1f, -1f, 1f, 1f, -1f)

        normToViewMatrix(source).mapPoints(corners)

        canvas.drawLine(corners[0], corners[1], corners[2], corners[3], paint)
        canvas.drawLine(corners[2], corners[3], corners[4], corners[5], paint)
        canvas.drawLine(corners[4], corners[5], corners[0], corners[1], paint)
    }

    override fun onTouchEvent(source: PluginScalableImageView, event: MotionEvent): Boolean {
        // show coordinates in norm-coordinates
        val matrix = viewToNormMatrix(source)

        val pt = floatArrayOf(event.x, event.y)
        matrix.mapPoints(pt)

        tv.text = "${pt[0]} x ${pt[1]}"

        return false
    }

    private fun normToViewMatrix(scalableImageView: ScalableImageView): Matrix {
        val bw = scalableImageView.bitmapModel.width.toFloat()
        val bh = scalableImageView.bitmapModel.height.toFloat()

        val matrix = Matrix(scalableImageView.scaleNormMatrix)
        matrix.postConcat(
            scalableImageView.bitmapModel.normToBitmapMatrix()
        )
        matrix.postConcat(
            scalableImageView.bitmapToViewMatrix()
        )

        return matrix
    }

    private fun viewToNormMatrix(scalableImageView: ScalableImageView): Matrix {
        return normToViewMatrix(scalableImageView).also {
            it.invert(it)
        }
    }
}