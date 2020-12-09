package at.searles.fractimageview.plugins

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.util.TypedValue
import android.view.MotionEvent
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.PluginScalableImageView
import kotlin.math.min

// TODO:
// * Plugins: Must draw and have access to
class GridPlugin(context: Context): Plugin {

    override var isEnabled: Boolean = true

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        val vw = source.width.toFloat()
        val vh = source.height.toFloat()

        val cx = vw / 2f
        val cy = vh / 2f
        val sbw = source.scaledBitmapWidth
        val sbh = source.scaledBitmapHeight

        val minLen = min(sbw, sbh) / 2f

        for(gridPaint in listOf(whiteBoldPaint, blackPaint)) {
            // outside grid
            canvas.drawLine(0f, cy - minLen, vw, cy - minLen, gridPaint)
            canvas.drawLine(0f, cy + minLen, vw, cy + minLen, gridPaint)
            canvas.drawLine(cx - minLen, 0f, cx - minLen, vh, gridPaint)
            canvas.drawLine(cx + minLen, 0f, cx + minLen, vh, gridPaint)

            // inside cross
            canvas.drawLine(0f, vh / 2f, vw, vh / 2f, gridPaint)
            canvas.drawLine(vw / 2f, 0f, vw / 2f, vh, gridPaint)

            // and circles inside
            canvas.drawCircle(cx, cy, minLen, gridPaint)
            canvas.drawCircle(cx, cy, minLen / 2f, gridPaint)
        }

        // and also draw quaters with thinner lines
        for(gridPaint in listOf(blackDashedPaint, whiteDashedPaint)) {
            canvas.drawLine(0f, cy - minLen / 2f, vw, cy - minLen / 2f, gridPaint)
            canvas.drawLine(0f, cy + minLen / 2f, vw, cy + minLen / 2f, gridPaint)
            canvas.drawLine(cx - minLen / 2f, 0f, cx - minLen / 2f, vh, gridPaint)
            canvas.drawLine(cx + minLen / 2f, 0f, cx + minLen / 2f, vh, gridPaint)
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

    private val widthPx = dpToPx(widthDp, context.resources)

    private val whiteBoldPaint = Paint().apply {
        color = 0xaaffffff.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx * 2
    }

    private val blackPaint = Paint().apply {
        color = 0xaa000000.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx
    }

    private val whilePaint = Paint().apply {
        color = 0xaaffffff.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx
    }

    private val whiteDashedPaint = Paint().apply {
        color = 0xaaffffff.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx
        pathEffect = DashPathEffect(floatArrayOf(widthPx, widthPx * 3), 0f)
    }

    private val blackDashedPaint = Paint().apply {
        color = 0xaa000000.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx
        pathEffect = DashPathEffect(floatArrayOf(widthPx, widthPx * 3), widthPx * 2)
    }

    companion object {
        private const val widthDp = 1f
    }
}