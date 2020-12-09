package at.searles.paletteeditor.paletteeditorview

import android.graphics.Canvas
import android.view.MotionEvent
import at.searles.multiscrollview.InnerPaneView
import at.searles.multiscrollview.ScrollDirection
import at.searles.paletteeditor.R

class VerticalOffsetPane(rootView: InnerPaneView, private val paletteEditorPane: PaletteEditorPane): OffsetPane(rootView, paletteEditorPane) {
    override val width: Float
        get() = paletteEditorPane.iconSize
    override val height: Float
        get() = paletteEditorPane.height - paletteEditorPane.spacing

    override val scrollDirection = ScrollDirection.Vertical

    private fun offsetFromY(y: Float, visibleY0: Float) = (y + visibleY0 - sliderIconSize / 2f) / (height - sliderIconSize)

    lateinit var listener: Listener

    override fun updateOffsetFromEvent(e: MotionEvent, visibleX0: Float, visibleY0: Float) {
        listener.onVerticalOffsetChanged(offsetFromY(e.y, visibleY0))
    }

    override fun sliderIconX(visibleX0: Float) = width / 2f
    override fun sliderIconY(visibleY0: Float) = model.offsetY * height - visibleY0

    override val sliderIcon: Int = R.drawable.ic_slider_pointer_vertical_24dp

    override fun drawRuler(canvas: Canvas, visibleX0: Float, visibleY0: Float) {
        val start = 0f - visibleY0
        val mid0 = sliderIconY(visibleY0) - sliderIconSize / 2f
        val mid1 = sliderIconY(visibleY0) + sliderIconSize / 2f
        val end = height - visibleY0

        if(start < mid0) {
            sliderRulerPaint.color = activeColor
            canvas.drawLine(width / 2f, start, width / 2f, mid0, sliderRulerPaint)
        }

        if(mid1 < end) {
            sliderRulerPaint.color = passiveColor
            canvas.drawLine(width / 2f, mid1, width / 2f, end, sliderRulerPaint)
        }
    }

    interface Listener {
        fun onVerticalOffsetChanged(offset: Float)
    }
}