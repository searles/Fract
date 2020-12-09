package at.searles.paletteeditor.paletteeditorview

import android.graphics.Canvas
import at.searles.multiscrollview.InnerPaneView
import at.searles.paletteeditor.R

class HorizontalEditTablePane(rootView: InnerPaneView, private val paletteEditorPane: PaletteEditorPane): EditTablePane(rootView, paletteEditorPane) {

    override val width: Float
        get() = paletteEditorPane.width
    override val height: Float
        get() = iconSize + spacing

    lateinit var listener: Listener

    override fun plusClicked() {
        listener.addRow()
    }

    override fun minusClicked() {
        listener.removeColumn()
    }

    override val plusDrawable: Int = R.drawable.ic_add_circle_arrow_left_24dp
    override val minusDrawable: Int = R.drawable.ic_minus_circle_arrow_up_24dp

    override fun centerPlusX(visibleX0: Float) = 0f - visibleX0 - iconSize / 2f
    override fun centerPlusY(visibleY0: Float) = spacing - visibleY0 + iconSize / 2f

    override fun centerMinusX(visibleX0: Float) = width - visibleX0 - iconSize / 2f
    override fun centerMinusY(visibleY0: Float) = height - visibleY0 - iconSize / 2f - spacing

    override fun onDraw(canvas: Canvas, visibleX0: Float, visibleY0: Float, visibleWidth: Float, visibleHeight: Float) {
        val colorRangeEnd = if(model.columnCount > 1) model.columnCount - 1 else 1

        drawColorRange(canvas, 0 until colorRangeEnd, 0 .. 0, visibleX0, visibleY0 - spacing)

        drawPlus(canvas, centerPlusX(visibleX0), centerPlusY(visibleY0))

        if(model.columnCount > 1) {
            drawMinus(canvas, centerMinusX(visibleX0), centerMinusY(visibleY0))
        }
    }

    interface Listener {
        fun addRow()
        fun removeColumn()
    }
}
