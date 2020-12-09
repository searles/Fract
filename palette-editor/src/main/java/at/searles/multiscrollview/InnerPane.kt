package at.searles.multiscrollview

import android.graphics.Canvas
import android.view.DragEvent
import android.view.MotionEvent

interface InnerPane {
    val width: Float
    val height: Float

    fun onScrollTo(e: MotionEvent, visibleX0: Float, visibleY0: Float): ScrollDirection?

    fun onClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun onLongPress(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun onTapDown(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun onTapUp(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun onDoubleClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean

    fun onDraw(canvas: Canvas, visibleX0: Float, visibleY0: Float, visibleWidth: Float, visibleHeight: Float)

    fun dragStarted(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun dragEntered(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun dragExited(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun dragLocation(e: DragEvent, visibleX0: Float, visibleY0: Float): ScrollDirection?
    fun dragEnded(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean
    fun drop(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean
}