package at.searles.paletteeditor

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import at.searles.colorpicker.dialog.ColorDialogCallback
import at.searles.colorpicker.dialog.ColorDialogFragment
import at.searles.paletteeditor.paletteeditorview.*


class PaletteEditorController(private val activity: AppCompatActivity, private val model: PaletteEditorModel): PaletteEditorPane.Listener, VerticalOffsetPane.Listener, HorizontalOffsetPane.Listener, VerticalEditTablePane.Listener, HorizontalEditTablePane.Listener, ColorDialogCallback {
    private fun inRange(col: Int, row: Int): Boolean {
        return 0 <= col && col < model.columnCount && 0 <= row && row < model.rowCount
    }

    override fun editColorPointAt(col: Int, row: Int) {
        require(inRange(col, row))

        val bundle = Bundle().apply {
            putInt(rowKey, row)
            putInt(columnKey, col)
        }

        ColorDialogFragment.newInstance(model.colorAt(col, row), bundle).
            show(activity.supportFragmentManager, "dialog")
    }

    override fun setColor(dialogFragment: ColorDialogFragment, color: Int) {
        val row = dialogFragment.arguments!!.getInt(rowKey)
        val col = dialogFragment.arguments!!.getInt(columnKey)

        model.setColorPoint(col, row, color)
    }

    override fun removeColorPointAt(col: Int, row: Int) {
        require(inRange(col, row))

        model.removeColorPoint(col, row)
    }

    override fun addColorPointAt(col: Int, row: Int, color: Int) {
        model.setColorPoint(col, row, color)
    }

    override fun selectColorAt(col: Int, row: Int) {
        model.setSelection(col, row)
    }

    override fun unselectColor() {
        model.setSelection(-1, -1)
    }

    override fun moveColorPoint(fromCol: Int, fromRow: Int, toCol: Int, toRow: Int) {
        require(inRange(fromCol, fromRow) && inRange(toCol, toRow))

        if(fromCol == toCol && fromRow == toRow) {
            return
        }

        val color = model.colorAt(fromCol, fromRow)

        if(model.isColorPoint(fromCol, fromRow) && !model.isColorPoint(toCol, toRow)) {
            model.removeColorPoint(fromCol, fromRow)
        }

        model.setColorPoint(toCol, toRow, color)
    }

    override fun onVerticalOffsetChanged(offset: Float) {
        model.offsetY = offset
    }

    override fun onHorizontalOffsetChanged(offset: Float) {
        model.offsetX = offset
    }

    override fun addColumn() {
        model.columnCount++
    }

    override fun removeRow() {
        if(model.rowCount > 1) {
            model.rowCount--
        }
    }

    override fun addRow() {
        model.rowCount++
    }

    override fun removeColumn() {
        if(model.columnCount > 1) {
            model.columnCount--
        }
    }

    companion object {
        private const val rowKey = "row"
        private const val columnKey = "column"
    }
}