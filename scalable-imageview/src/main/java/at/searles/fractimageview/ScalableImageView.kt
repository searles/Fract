package at.searles.fractimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

open class ScalableImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    var hasRotationLock = false
        set(value) {
            cancelMultitouchGesture()
            field = value
        }

    var mustConfirmZoom = false
        set(value) {
            cancelMultitouchGesture()
            field = value
        }

    var hasCenterLock = false
        set(value) {
            cancelMultitouchGesture()
            field = value
        }

    // TODO: Add method that normalizes coordinates but respects current scale matrix.

    /* Gesture control:
     * - scroll events are forwarded to a multitouch controller
     * - double tabs are zooms at the current position.
     */

    /**
     * To detect gestures (3 finger drag etc...)
     */
    private val detector = GestureDetector(context, ScaleGestureListener())

    /**
     * Adapter to convert gestures to input for the multitouch controller
     */
    private var multitouchAdapter: GestureToMultiTouchAdapter? = null

    lateinit var bitmapModel: ScalableBitmapModel

    private val identityMatrix = Matrix()

    val scaleNormMatrix: Matrix
        get() = multitouchAdapter?.normMatrix ?: identityMatrix

    val imageMatrix: Matrix
        get() {
            return bitmapInViewMatrix(
                bitmapModel.bitmapTransformMatrix, scaleNormMatrix
            )
        }

    override fun onDraw(canvas: Canvas) {
        // draw image
        canvas.drawBitmap(bitmapModel.bitmap, imageMatrix, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_CANCEL -> {
                if(cancelMultitouchGesture()) {
                    return true
                }
            }
            MotionEvent.ACTION_DOWN -> {
                if(multitouchAdapter == null) {
                    multitouchAdapter = GestureToMultiTouchAdapter()
                }

                multitouchAdapter?.apply {
                    down(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                multitouchAdapter?.run{
                    up(event)
                    if(isActive) {
                        if(!mustConfirmZoom) commitMultitouchGesture()
                    }
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                multitouchAdapter?.down(event)
            }
            MotionEvent.ACTION_POINTER_UP -> {
                multitouchAdapter?.up(event)
            }
        }

        return detector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    fun cancelMultitouchGesture(): Boolean {
        if(multitouchAdapter != null) {
            multitouchAdapter = null
            invalidate()
            return true
        }

        return false
    }

    private fun commitMultitouchGesture() {
        val scaleMatrix = multitouchAdapter!!.normMatrix

        if(!scaleMatrix.isIdentity) {
            bitmapModel.scale(scaleMatrix)
        }

        multitouchAdapter = null
        invalidate()
    }

    internal inner class ScaleGestureListener: SimpleOnGestureListener() {
        override fun onDown(motionEvent: MotionEvent): Boolean { // must be true, otherwise no touch events.
            return true
        }

        override fun onDoubleTap(event: MotionEvent): Boolean {
            if (mustConfirmZoom && multitouchAdapter != null && multitouchAdapter!!.isActive) { // not active when 'confirm zoom' is set.
                return false
            }

            if (multitouchAdapter != null) {
                cancelMultitouchGesture()
            }

            val index = event.actionIndex
            val p = PointF(event.getX(index), event.getY(index))
            val np = norm(p)

            bitmapModel.scale(createScaleMatrix(np, dtScaleFactor, hasCenterLock))

            return true
        }

        override fun onSingleTapUp(motionEvent: MotionEvent): Boolean {
            return if (mustConfirmZoom && multitouchAdapter?.isActive == true) {
                commitMultitouchGesture()
                true
            } else {
                cancelMultitouchGesture()
                false
            }
        }

        override fun onScroll(startEvt: MotionEvent?, currentEvt: MotionEvent, vx: Float, vy: Float): Boolean {
            if(multitouchAdapter != null) {
                multitouchAdapter!!.scroll(currentEvt)
                return true
            }

            return false
        }
    }

    fun bitmapToViewMatrix(ret: Matrix = Matrix()): Matrix {
        val bw = bitmapModel.width.toFloat()
        val bh = bitmapModel.height.toFloat()
        val vw = width.toFloat()
        val vh = height.toFloat()

        require(bw > 0 && bh > 0 && vw > 0 && vh > 0)

        val vr = RectF(0f, 0f, vw, vh)

        // next must be coordinated with isBitmapFlipped.
        val br = if (vw > vh) {
            RectF(0f, 0f, max(bw, bh), min(bw, bh))
        } else {
            RectF(0f, 0f, min(bw, bh), max(bw, bh))
        }

        val m = ret.apply {
            setRectToRect(br, vr, Matrix.ScaleToFit.CENTER)
        }

        // Check orientation
        if (isBitmapFlipped) { // Turn centerImageMatrix by 90 degrees
            m.preTranslate(bh, 0f)
            m.preRotate(90f)
        }

        return m
    }

    val isBitmapFlipped: Boolean
        get() {
            val bw = bitmapModel.width
            val bh = bitmapModel.height
            val vw = width
            val vh = height

            // maximize filled area
            return bw > bh && vw < vh || bw < bh && vw > vh
        }

    /**
     * This returns the up-scaled length of the bitmap's edge towards the view's width.
     */
    val scaledBitmapWidth: Float
        get () {
            val bw = bitmapModel.width.toFloat()
            val bh = bitmapModel.height.toFloat()
            val vw = width.toFloat()
            val vh = height.toFloat()

            /* Just some thoughts:
            The scaled rectangle of the bitmap should fit into the view.
            So, the ratio is the min-ratio.
             */
            return if (isBitmapFlipped) {
                val ratio = min(vw / bh, vh / bw)
                bh * ratio
            } else {
                val ratio = min(vw / bw, vh / bh)
                bw * ratio
            }
        }

    val scaledBitmapHeight: Float
        get() {
            val bw = bitmapModel.width.toFloat()
            val bh = bitmapModel.height.toFloat()
            val vw = width.toFloat()
            val vh = height.toFloat()

            /* Just some thoughts:
            The scaled rectangle of the bitmap should fit into the view.
            So, the ratio is the min-ratio.
             */
            return if (isBitmapFlipped) {
                val ratio = min(vw / bh, vh / bw)
                bw * ratio
            } else {
                val ratio = min(vw / bw, vh / bh)
                bh * ratio
            }
    }

    /**
     * Normalize view coordinates using the provided bitmap coordinates. This one
     * takes care of isBitmapFlipped etc...
     */
    fun norm(p: PointF, ret: PointF = PointF()): PointF {
        val vw = width.toFloat()
        val vh = height.toFloat()

        val m = min(scaledBitmapWidth, scaledBitmapHeight)

        val x: Float
        val y: Float

        if(isBitmapFlipped) {
            x = (p.y * 2f - vh) / m
            y = (vw - p.x * 2f) / m
        } else {
            x = (p.x * 2f - vw) / m
            y = (p.y * 2f - vh) / m
        }

        return ret.apply {
            this.x = x
            this.y = y
        }
    }

    /**
     * Normalize view coordinates using the provided bitmap coordinates. This one
     * takes care of isBitmapFlipped etc...
     */
    fun invNorm(p: PointF, ret: PointF = PointF()): PointF {
        val vw = width.toFloat()
        val vh = height.toFloat()

        val m = min(scaledBitmapWidth, scaledBitmapHeight)

        val x: Float
        val y: Float

        if(isBitmapFlipped) {
            y = (p.x * m + vh) / 2f
            x = (vw - p.y * m) / 2f
        } else {
            x = (p.x * m + vw) / 2f
            y = (p.y * m + vh) / 2f
        }

        return ret.apply {
            this.x = x
            this.y = y
        }
    }

    fun bitmapInViewMatrix(bitmapNormMatrix: Matrix, scaleNormMatrix: Matrix, ret: Matrix = Matrix()): Matrix {
        return ret.apply {
            set(scaleNormMatrix)
            preConcat(bitmapNormMatrix)

            preConcat(bitmapModel.bitmapToNormMatrix())
            postConcat(bitmapModel.normToBitmapMatrix())
            postConcat(bitmapToViewMatrix())
        }
    }



    internal inner class GestureToMultiTouchAdapter {
        private val controller = MultiTouchController(hasRotationLock, hasCenterLock)

        var isActive = false

        val normMatrix: Matrix
            get() = controller.matrix

        fun down(event: MotionEvent) {
            val index = event.actionIndex
            val id = event.getPointerId(index)
            val pt = norm(PointF(event.getX(index), event.getY(index)))

            controller.pointDown(id, pt)
        }

        fun up(event: MotionEvent) {
            val index = event.actionIndex
            val id = event.getPointerId(index)
            controller.pointUp(id)
        }

        fun scroll(event: MotionEvent) {
            isActive = true
            for (index in 0 until event.pointerCount) {
                val pt = norm(PointF(event.getX(index), event.getY(index)))
                val id = event.getPointerId(index)
                controller.pointDrag(id, pt)
            }

            invalidate()
        }
    }

    companion object {
        /**
         * Scale factor on double tapping
         */
        const val dtScaleFactor = 3f

        fun createScaleMatrix(center: PointF, factor: Float, isCenterLock: Boolean, ret: Matrix = Matrix()): Matrix {
            return ret.apply {
                setValues(
                    floatArrayOf(
                        factor, 0f, if(isCenterLock) 0f else center.x * (1 - factor),
                        0f, factor, if(isCenterLock) 0f else center.y * (1 - factor),
                        0f, 0f, 1f
                    )
                )
            }
        }
    }
}