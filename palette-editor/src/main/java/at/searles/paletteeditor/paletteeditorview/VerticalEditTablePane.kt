package at.searles.paletteeditor.paletteeditorview

import android.graphics.Canvas
import at.searles.multiscrollview.InnerPaneView
import at.searles.paletteeditor.R

// Name: Viewport.

class VerticalEditTablePane(rootView: InnerPaneView, private val paletteEditorPane: PaletteEditorPane): EditTablePane(rootView, paletteEditorPane) {
    override val width: Float
        get() = iconSize + spacing
    override val height: Float
        get() = paletteEditorPane.height

    lateinit var listener: Listener

    override fun plusClicked() {
        listener.addColumn()
    }

    override fun minusClicked() {
        listener.removeRow()
    }

    override val plusDrawable: Int = R.drawable.ic_add_circle_arrow_down_24dp
    override val minusDrawable: Int = R.drawable.ic_minus_circle_arrow_right_24dp

    override fun centerPlusX(visibleX0: Float) = spacing + iconSize / 2f - visibleX0
    override fun centerPlusY(visibleY0: Float) = 0f - visibleY0 - iconSize / 2f

    override fun centerMinusX(visibleX0: Float) = iconSize / 2f - visibleX0
    override fun centerMinusY(visibleY0: Float) = height - visibleY0 - iconSize / 2f

    override fun onDraw(canvas: Canvas, visibleX0: Float, visibleY0: Float, visibleWidth: Float, visibleHeight: Float) {
        val colorRangeEnd = if(model.rowCount > 1) model.rowCount - 1 else 1

        drawColorRange(canvas, 0 .. 0, 0 until colorRangeEnd, visibleX0 - spacing, visibleY0)

        drawPlus(canvas, centerPlusX(visibleX0), centerPlusY(visibleY0))

        if(model.rowCount > 1) {
            drawMinus(canvas, centerMinusX(visibleX0), centerMinusY(visibleY0))
        }
    }

    interface Listener {
        fun addColumn()
        fun removeRow()
    }
}
