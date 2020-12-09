package at.searles.paletteeditor.paletteeditorview

import android.content.ClipData
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import at.searles.commons.color.Colors
import at.searles.multiscrollview.InnerPane
import at.searles.multiscrollview.InnerPaneView
import at.searles.multiscrollview.ScrollDirection
import at.searles.paletteeditor.Dpis
import at.searles.paletteeditor.PaletteEditorModel
import at.searles.paletteeditor.R
import at.searles.paletteeditor.ThemeUtils
import kotlin.math.max
import kotlin.math.min

class PaletteEditorPane(private val rootView: InnerPaneView, val model: PaletteEditorModel): InnerPane {

    val iconSize: Float = Dpis.dpiToPx(
        rootView.resources,
        defaultIconSizeDp
    )

    val spacing: Float = Dpis.dpiToPx(
        rootView.resources,
        defaultSpacingDp
    )
    
    lateinit var listener: Listener

    override val width: Float
        get() = model.columnCount * (iconSize + spacing)

    override val height: Float
        get() = model.rowCount * (iconSize + spacing)

    private val colorRectPaint = Paint()

    override fun onDoubleClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        val col = columnAt(e.x, visibleX0)
        val row = rowAt(e.y, visibleY0)

        if(col < 0 || model.columnCount <= col || row < 0 || model.rowCount <= row) {
            return false
        }

        listener.editColorPointAt(col, row)

        return true
    }

    private val draggableView = Button(rootView.context).also {
        it.layoutParams = RelativeLayout.LayoutParams(
            iconSize.toInt(),
            iconSize.toInt()
        )

        it.visibility = View.INVISIBLE

        rootView.addView(it)
    }

    private fun isInRange(col: Int, row: Int): Boolean {
        return 0 <= col && col < model.columnCount && 0 <= row && row < model.rowCount
    }

    override fun onTapDown(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onScrollTo(e: MotionEvent, visibleX0: Float, visibleY0: Float): ScrollDirection? {
        return null
    }

    override fun onTapUp(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    private var isColorMoved = false

    private var moveFromCol = 0
    private var moveFromRow = 0

    override fun onLongPress(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        val col = columnAt(e.x, visibleX0)
        val row = rowAt(e.y, visibleY0)

        if(!isInRange(col, row)) {
            return false
        }

        val selectedColor = model.colorAt(col, row)

        val dragLoad = createClipDataFromColor(model.colorAt(col, row))

        draggableView.backgroundTintList = ColorStateList.valueOf(selectedColor)
        val shadowBuilder = View.DragShadowBuilder(draggableView)
        draggableView.startDragAndDrop(dragLoad, shadowBuilder, draggableView, 0)

        isColorMoved = model.isColorPoint(col, row)
        isDragRemoveAction = false

        moveFromCol = col
        moveFromRow = row

        return true
    }

    private var isDragRemoveAction = false

    override fun dragStarted(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return true
    }

    override fun dragEntered(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return true
    }

    override fun dragExited(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        isDragRemoveAction = true
        return true
    }

    override fun dragLocation(e: DragEvent, visibleX0: Float, visibleY0: Float): ScrollDirection? {
        val col = columnAt(e.x, visibleX0)
        val row = rowAt(e.y, visibleY0)

        isDragRemoveAction = !isInRange(col, row)

        if(!isDragRemoveAction) {
            listener.selectColorAt(col, row)
        } else {
            listener.unselectColor()
        }

        rootView.invalidate()

        return ScrollDirection.Both
    }

    override fun drop(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        val col = columnAt(e.x, visibleX0)
        val row = rowAt(e.y, visibleY0)
        listener.unselectColor()

        isDragRemoveAction = !isInRange(col, row)

        if(isColorMoved && !isDragRemoveAction) {
            listener.moveColorPoint(moveFromCol, moveFromRow, col, row)
            isColorMoved = false
            return true
        }

        if(isColorMoved) {
            isColorMoved = false
            isDragRemoveAction = false

            if(model.isColorPoint(moveFromCol, moveFromRow)) {
                listener.removeColorPointAt(moveFromCol, moveFromRow)
                return true
            }

            return false
        }

        if(!isDragRemoveAction) { // set color from palette
            val color = getColorFromClipData(e.clipData)
            listener.addColorPointAt(col, row, color)
            return true
        }

        isDragRemoveAction = false
        return false
    }

    override fun dragEnded(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        listener.unselectColor() // can happen if in a large table a fast drag moves to another view

        if(isColorMoved) {
            isColorMoved = false

            // a color was dragged out of range, this means, delete it.

            listener.removeColorPointAt(moveFromCol, moveFromRow)
            rootView.invalidate()
            return true
        }

        rootView.invalidate()
        return true
    }

    private fun columnAt(x: Float, visibleX0: Float): Int = ((x + visibleX0) / (iconSize + spacing) + 1f).toInt() - 1
    private fun rowAt(y: Float, visibleY0: Float): Int = ((y + visibleY0) / (iconSize + spacing) + 1f).toInt() - 1

    fun x0At(col: Int, visibleX0: Float) = (col * (iconSize + spacing) - visibleX0)
    fun y0At(row: Int, visibleY0: Float) = (row * (iconSize + spacing) - visibleY0)

    override fun onDraw(canvas: Canvas, visibleX0: Float, visibleY0: Float, visibleWidth: Float, visibleHeight: Float) {
        val startCol = max(0, min(model.columnCount - 1, columnAt(0f, visibleX0)))
        val endCol = max(0, min(model.columnCount - 1, columnAt(visibleWidth, visibleX0)))

        val startRow = max(0, min(model.rowCount - 1, rowAt(0f, visibleY0)))
        val endRow = max(0, min(model.rowCount - 1, rowAt(visibleHeight, visibleY0)))

        for(row in startRow .. endRow) {
            for (col in startCol..endCol) {
                drawColor(canvas, col, row, visibleX0, visibleY0)
            }
        }
    }

    fun drawColorAt(canvas: Canvas, x0: Float, y0: Float, color: Int) {
        colorRectPaint.color = color
        canvas.drawRect(x0, y0, x0 + iconSize, y0 + iconSize, colorRectPaint)
    }

    private fun isLightOverlayColor(color: Int): Boolean {
        return Colors.brightness(color) < 0.25f
    }

    private val selectedOverlayColor = Colors.transparent(transparency, ThemeUtils.getThemeAccentColor(rootView.context))
    private val deleteIconBlack: Drawable = rootView.resources.getDrawable(R.drawable.ic_clear_black_24dp, null)
    private val pointIconBlack: Drawable = rootView.resources.getDrawable(R.drawable.ic_check_black_24dp, null)
    private val deleteIconWhite: Drawable = rootView.resources.getDrawable(R.drawable.ic_clear_white_24dp, null)
    private val pointIconWhite: Drawable = rootView.resources.getDrawable(R.drawable.ic_check_white_24dp, null)

    private fun drawColor(canvas: Canvas, col: Int, row: Int, visibleX0: Float, visibleY0: Float) {
        val x0 = x0At(col, visibleX0)
        val y0 = y0At(row, visibleY0)

        val isColorPoint = model.isColorPoint(col, row)
        val color = model.colorAt(col, row)
        drawColorAt(canvas, x0, y0, color)

        if(isColorPoint) {
            val overlayIcon: Drawable = if(isColorMoved && moveFromCol == col && moveFromRow == row) {
                if(isLightOverlayColor(color))
                    deleteIconWhite
                else
                    deleteIconBlack
            } else {
                if(isLightOverlayColor(color))
                    pointIconWhite
                else
                    pointIconBlack
            }

            overlayIcon.setBounds(x0.toInt(), y0.toInt(), (x0 + iconSize).toInt(), (y0 + iconSize).toInt())
            overlayIcon.draw(canvas)
        }

        if(model.selectedCol == col && model.selectedRow == row) {
            colorRectPaint.color = selectedOverlayColor
            canvas.drawRect(x0 - spacing / 2f, y0 - spacing / 2f, x0 + iconSize + spacing / 2f, y0 + iconSize + spacing / 2f, colorRectPaint)
        }
    }

    interface Listener {
        fun selectColorAt(col: Int, row: Int)
        fun unselectColor()

        fun moveColorPoint(fromCol: Int, fromRow: Int, toCol: Int, toRow: Int)
        fun addColorPointAt(col: Int, row: Int, color: Int)
        fun removeColorPointAt(col: Int, row: Int)

        fun editColorPointAt(col: Int, row: Int)
    }

    companion object {
        const val transparency = 0.26f

        const val defaultIconSizeDp = 48f
        const val defaultSpacingDp = 2f
        private const val colorStringKey = "colorString"

        fun createClipDataFromColor(color: Int): ClipData {
            return ClipData.newPlainText(colorStringKey, Colors.toColorString(color))
        }

        fun getColorFromClipData(clipData: ClipData): Int {
            return Colors.fromColorString(clipData.getItemAt(0).text.toString())
        }
    }
}