package at.searles.paletteeditor.paletteeditorview

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.DragEvent
import android.view.MotionEvent
import at.searles.commons.color.Colors
import at.searles.multiscrollview.InnerPane
import at.searles.multiscrollview.InnerPaneView
import at.searles.multiscrollview.ScrollDirection
import at.searles.paletteeditor.Dpis
import kotlin.math.hypot

abstract class EditTablePane(private val rootView: InnerPaneView, private val paletteEditorPane: PaletteEditorPane): InnerPane {
    protected val iconSize
        get() = paletteEditorPane.iconSize

    val model
        get() = paletteEditorPane.model

    protected val spacing: Float = Dpis.dpiToPx(rootView.resources, spacingDp)

    protected abstract fun plusClicked()
    protected abstract fun minusClicked()

    override fun onClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        // is it +?
        if(2f * hypot((centerPlusX(visibleX0) - e.x).toDouble(), (centerPlusY(visibleY0) - e.y).toDouble()) < iconSize) {
            plusClicked()
            return true
        }

        // is it -?
        if(2f * hypot(centerMinusX(visibleX0) - e.x, centerMinusY(visibleY0) - e.y) < iconSize) {
            minusClicked()
            return true
        }

        return false
    }

    override fun onScrollTo(e: MotionEvent, visibleX0: Float, visibleY0: Float): ScrollDirection {
        return ScrollDirection.NoScroll
    }

    override fun onLongPress(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onTapDown(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onTapUp(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onDoubleClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragStarted(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragEntered(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragExited(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragLocation(e: DragEvent, visibleX0: Float, visibleY0: Float): ScrollDirection? {
        return null
    }

    override fun dragEnded(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun drop(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    abstract val plusDrawable: Int
    abstract val minusDrawable: Int

    fun drawPlus(canvas: Canvas, x: Float, y: Float) {
        val d: Drawable = rootView.resources.getDrawable(plusDrawable, null)
        d.setBounds((x - iconSize / 2f).toInt(),
            (y - iconSize / 2f).toInt(), (x + iconSize / 2f).toInt(), (y + iconSize / 2f).toInt())
        d.draw(canvas)
    }

    fun drawMinus(canvas: Canvas, x: Float, y: Float) {
        val d: Drawable = rootView.resources.getDrawable(minusDrawable, null)
        d.setBounds((x - iconSize / 2f).toInt(),
            (y - iconSize / 2f).toInt(), (x + iconSize / 2f).toInt(), (y + iconSize / 2f).toInt())
        d.draw(canvas)
    }

    abstract fun centerPlusX(visibleX0: Float): Float
    abstract fun centerPlusY(visibleY0: Float): Float

    abstract fun centerMinusX(visibleX0: Float): Float
    abstract fun centerMinusY(visibleY0: Float): Float

    fun drawColorRange(canvas: Canvas, cols: IntRange, rows: IntRange, visibleX0: Float, visibleY0: Float) {
        for(row in rows) {
            for(col in cols) {
                val x0 = paletteEditorPane.x0At(col, visibleX0)
                val y0 = paletteEditorPane.y0At(row, visibleY0)

                val color = Colors.transparent(transparency, model.colorAt(col, row))
                paletteEditorPane.drawColorAt(canvas, x0, y0, color)
            }
        }
    }

    companion object {
        const val transparency = 0.26f
        const val spacingDp = 8f
    }
}