package at.searles.fract.plugins

import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.util.TypedValue
import at.searles.commons.math.Cplx
import at.searles.fract.FractMainActivity
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractimageview.ScalableImageView

class OrbitPlugin(private val activity: FractMainActivity, private val source: ScalableImageView): MotionPlugin() {

    var orbit: List<Cplx> = emptyList()
        set(value) {
            field = value
            updatePath()
        }

    private var path: List<PointF> = emptyList()
        set(value) {
            field = value
            source.invalidate()
        }

    override var isEnabled = false
        set(value) {
            if(field == value) {
                return
            }

            field = value

            if(field) {
                path = emptyList()
                startPoint?.let { createOrbit(it) }
            }
        }

    private var startPoint: Cplx?
        set(value) {
            val pt = activity.getSettings().orbitStartPoint

            if(pt?.get(0) != value?.re() || pt?.get(1) != value?.im()) {
                activity.setSettings(
                    activity.getSettings().withOrbitStartPoint(value)
                )
            }
        }
        get() {
            return activity.getSettings().orbitStartPoint?.let {
                Cplx(it[0], it[1])
            }
        }

    override fun onLayoutChanged(source: PluginScalableImageView) {
        updatePath()
    }

    private fun updatePath() {
        path = orbit.map { scaledPtToPixel(it) }
    }

    override fun movePointer(source: PluginScalableImageView) {
        val pt = calculateStartPoint()
        createOrbit(pt)
        startPoint = pt
    }

    override fun deactivatePlugin() {
        // touch event ended
    }

    override fun activatePlugin(source: PluginScalableImageView) {
        val pt = calculateStartPoint()
        createOrbit(pt)
        startPoint = pt
    }

    private var bgTask: CalcOrbitTask? = null

    private fun calculateStartPoint(): Cplx {
        val bm = source.bitmapModel as FractBitmapModel
        val normPt = source.norm(PointF(currentTouchX, currentTouchY))
        val scaledPt = bm.properties.scale.scalePoint(normPt.x.toDouble(), normPt.y.toDouble(), DoubleArray(2))

         return Cplx(scaledPt[0], scaledPt[1])
    }

    private fun createOrbit(startPt: Cplx) {
        bgTask?.cancel(false)

        val bm = source. bitmapModel as FractBitmapModel

        bgTask = CalcOrbitTask(
            startPt,
            bm.properties.program,
            this
        ).apply {
            execute()
        }
    }

    fun calculateOrbit() {
        path = emptyList()
        startPoint?.let {
            createOrbit(it)
        }
    }

    private fun scaledPtToPixel(c: Cplx): PointF {
        val bm = source. bitmapModel as FractBitmapModel
        val normPt = bm.properties.scale.invScalePoint(c.re(), c.im(), DoubleArray(2))
        return source.invNorm(PointF(normPt[0].toFloat(), normPt[1].toFloat()))
    }

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        val startPt = startPoint ?: return

        if(path.isEmpty()) {
            return
        }

        val path = this.path

        val showPointsBounds = RectF(-source.width.toFloat(), -source.height.toFloat(),
            2f * source.width.toFloat(), 2 * source.height.toFloat())

        val startPointCoords = scaledPtToPixel(startPt)

        canvas.drawCircle(startPointCoords.x, startPointCoords.y, widthPx * 2, blackFillPaint)
        canvas.drawCircle(startPointCoords.x, startPointCoords.y, widthPx * 2, whitePaint)

        var pt0 = path.first()

        path.drop(1).forEach {
            if(showPointsBounds.contains(pt0.x, pt0.y) || showPointsBounds.contains(it.x, it.y)) {
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

    private val widthPx = dpToPx(widthDp, activity.resources)

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