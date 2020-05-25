package at.searles.fract.plugins

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.TypedValue
import at.searles.commons.math.Cplx
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractimageview.ScalableImageView

class OrbitPlugin(context: Context, private val source: ScalableImageView): MotionPlugin() {

    var path: List<PointF> = emptyList()

    override var isEnabled = false
        set(value) {
            field = value
            updateOrbitIfEnabled()
        }

    init {
        source.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateOrbitIfEnabled() }
    }

    override fun onLayoutChanged(source: PluginScalableImageView) {
        updateOrbitIfEnabled()
    }

    private fun updateOrbitIfEnabled() {
        if(isEnabled && startPoint != null) {
            path = emptyList()
            createOrbit()
        }
    }

    override fun movePointer(source: PluginScalableImageView) {
        calcStartPoint()
        createOrbit()
    }

    override fun deactivatePlugin() {
        // touch event ended
    }

    override fun activatePlugin(source: PluginScalableImageView) {
        calcStartPoint()
        createOrbit()
    }

    private var bgTask: CalcOrbitTask? = null
    private var startPoint: Cplx? = null

    private fun calcStartPoint() {
        val bm = source.bitmapModel as FractBitmapModel

        val normPt = source.norm(PointF(currentTouchX, currentTouchY))

        val scaledPt = bm.properties.scale.scalePoint(normPt.x.toDouble(), normPt.y.toDouble(), DoubleArray(2))

        startPoint = Cplx(scaledPt[0], scaledPt[1])
    }

    private fun createOrbit() {
        bgTask?.cancel(false)

        val bm = source. bitmapModel as FractBitmapModel

        bgTask = CalcOrbitTask(
            startPoint!!,
            bm.properties.program,
            this
        ).apply {
            execute()
        }
    }

    private fun scaledPtToPixel(c: Cplx): PointF {
        val bm = source. bitmapModel as FractBitmapModel

        val normPt = bm.properties.scale.invScalePoint(c.re(), c.im(), DoubleArray(2))

        return source.invNorm(PointF(normPt[0].toFloat(), normPt[1].toFloat()))
    }

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        val rect = RectF(-source.width.toFloat(), -source.height.toFloat(),
            2f * source.width.toFloat(), 2 * source.height.toFloat())

        val path = this.path

        if(startPoint == null || path.isEmpty()) {
            return
        }

        val startPointCoords = scaledPtToPixel(startPoint!!)

        canvas.drawCircle(startPointCoords.x, startPointCoords.y, widthPx * 2, blackFillPaint)
        canvas.drawCircle(startPointCoords.x, startPointCoords.y, widthPx * 2, whitePaint)

        var pt0 = path.first()

        path.drop(1).forEach {
            if(rect.contains(pt0.x, pt0.y) || rect.contains(it.x, it.y)) {
                canvas.drawLine(pt0.x, pt0.y, it.x, it.y, blackBoldPaint)
                canvas.drawLine(pt0.x, pt0.y, it.x, it.y, whitePaint)
            }

            pt0 = it
        }

        // Draw start and end.
        canvas.drawCircle(path.first().x, path.first().y, widthPx * 3, whiteFillPaint)
        canvas.drawCircle(path.first().x, path.first().y, widthPx * 3, blackPaint)

        canvas.drawCircle(path.last().x, path.last().y, widthPx * 3, whiteFillPaint)
        canvas.drawCircle(path.last().x, path.last().y, widthPx * 3, blackPaint)

        canvas.drawCircle(path.first().x, path.first().y, widthPx, blackFillPaint)
        canvas.drawCircle(path.last().x, path.last().y, widthPx * 2, blackThinPaint)
    }

    private fun dpToPx(dip: Float, resources: Resources): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            resources.displayMetrics
        )
    }

    fun setOrbit(orbit: List<Cplx>) {
        path = orbit.map { scaledPtToPixel(it) }
        source.invalidate()
    }

    private val widthPx = dpToPx(widthDp, context.resources)

    private val blackBoldPaint = Paint().apply {
        color = 0x80000000.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx * 2
    }

    private val blackPaint = Paint().apply {
        color = 0x80000000.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx
    }

    private val whitePaint = Paint().apply {
        color = 0x80ffffff.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx
    }

    private val blackFillPaint = Paint().apply {
        color = 0x80000000.toInt() // semi-transparent black
        style = Paint.Style.FILL
    }

    private val blackThinPaint = Paint().apply {
        color = 0x80000000.toInt() // semi-transparent black
        style = Paint.Style.STROKE
        strokeWidth = widthPx / 2
    }

    private val whiteFillPaint = Paint().apply {
        color = 0x80ffffff.toInt() // semi-transparent black
        style = Paint.Style.FILL
    }

    companion object {
        private const val widthDp: Float = 2f
    }
}