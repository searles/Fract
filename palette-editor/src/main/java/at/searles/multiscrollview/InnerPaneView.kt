package at.searles.multiscrollview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.DragEvent
import android.view.MotionEvent
import android.widget.RelativeLayout

class InnerPaneView(context: Context, attrs: AttributeSet): RelativeLayout(context, attrs) {

    init {
        setWillNotDraw(false)
    }

    var innerPane: InnerPane? = null
        set(value) {
            field = value
            requestLayout()
        }

    val intendedWidth: Float
        get() = innerPane?.width ?: 0f

    val intendedHeight: Float
        get() = innerPane?.height ?: 0f

    var visibleX0: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    var visibleY0: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    fun onScrollTo(e: MotionEvent): ScrollDirection {
        return innerPane?.onScrollTo(e, visibleX0, visibleY0) ?: ScrollDirection.NoScroll
    }

    fun onTapUp(e: MotionEvent): Boolean {
        return innerPane?.onTapUp(e, visibleX0, visibleY0) ?: false
    }

    fun onClick(e: MotionEvent): Boolean {
        return innerPane?.onClick(e, visibleX0, visibleY0) ?: false
    }

    fun onDoubleClick(e: MotionEvent): Boolean {
        return innerPane?.onDoubleClick(e, visibleX0, visibleY0) ?: false
    }

    fun onTapDown(e: MotionEvent): Boolean {
        return innerPane?.onTapDown(e, visibleX0, visibleY0) ?: false
    }

    fun onLongPress(e: MotionEvent) {
        innerPane?.onLongPress(e, visibleX0, visibleY0)
    }

    override fun onDraw(canvas: Canvas) {
        innerPane?.onDraw(canvas, visibleX0, visibleY0, width.toFloat(), height.toFloat())
    }

    fun dragStarted(e: DragEvent): Boolean {
        return innerPane?.dragStarted(e, visibleX0, visibleY0) ?: false
    }

    fun dragEntered(e: DragEvent): Boolean {
        return innerPane?.dragEntered(e, visibleX0, visibleY0) ?: false
    }

    fun dragExited(e: DragEvent): Boolean {
        return innerPane?.dragExited(e, visibleX0, visibleY0) ?: false
    }

    fun dragLocation(e: DragEvent): ScrollDirection? {
        return innerPane?.dragLocation(e, visibleX0, visibleY0)
    }

    fun dragEnded(e: DragEvent): Boolean {
        return innerPane?.dragEnded(e, visibleX0, visibleY0) ?: false
    }

    fun drop(e: DragEvent): Boolean {
        return innerPane?.drop(e, visibleX0, visibleY0) ?: false
    }
}