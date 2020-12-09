package at.searles.paletteeditor.paletteeditorview

import android.graphics.Canvas
import android.view.MotionEvent
import at.searles.multiscrollview.InnerPaneView
import at.searles.multiscrollview.ScrollDirection
import at.searles.paletteeditor.R

// Name: Viewport.

class HorizontalOffsetPane(rootView: InnerPaneView, private val paletteEditorPane: PaletteEditorPane): OffsetPane(rootView, paletteEditorPane) {
    override val width: Float
        get() = paletteEditorPane.width - paletteEditorPane.spacing

    override val height: Float
        get() = paletteEditorPane.iconSize

    override val scrollDirection = ScrollDirection.Horizontal

    private fun offsetFromX(x: Float, visibleX0: Float) = (x + visibleX0 - sliderIconSize / 2f) / (width - sliderIconSize)

    lateinit var listener: Listener

    override fun updateOffsetFromEvent(e: MotionEvent, visibleX0: Float, visibleY0: Float) {
        listener.onHorizontalOffsetChanged(offsetFromX(e.x, visibleX0))
    }

    override val sliderIcon: Int = R.drawable.ic_slider_pointer_horizontal_24dp

    override fun sliderIconX(visibleX0: Float) = model.offsetX * width - visibleX0
    override fun sliderIconY(visibleY0: Float) = height / 2f

    override fun drawRuler(canvas: Canvas, visibleX0: Float, visibleY0: Float) {
        val start = 0f - visibleX0
        val mid0 = sliderIconX(visibleX0) - sliderIconSize / 2f
        val mid1 = sliderIconX(visibleX0) + sliderIconSize / 2f
        val end = width - visibleX0

        if(start < mid0) {
            sliderRulerPaint.color = activeColor
            canvas.drawLine(start, height / 2f, mid0, height / 2f, sliderRulerPaint)
        }

        if(mid1 < end) {
            sliderRulerPaint.color = passiveColor
            canvas.drawLine(mid1, height / 2f, end, height / 2f, sliderRulerPaint)
        }
    }

    interface Listener {
        fun onHorizontalOffsetChanged(offset: Float)
    }
}