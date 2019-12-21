package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import java.lang.NumberFormatException


class ImageSizeDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.image_size_dialog)
            .setTitle(R.string.setImageSize)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { validateImageSize(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        if(savedInstanceState == null) {
            val width = arguments!!.getInt(widthKey)
            val height = arguments!!.getInt(heightKey)

            getWidthEditText(dialog).setText("$width")
            getHeightEditText(dialog).setText("$height")
        }

        return dialog
    }

    private fun getWidthEditText(dialog: Dialog) = dialog.findViewById<EditText>(R.id.widthEditText)!!
    private fun getHeightEditText(dialog: Dialog) = dialog.findViewById<EditText>(R.id.heightEditText)!!

    private fun validateImageSize() {
        try {
            val width = Integer.parseInt(getWidthEditText(dialog!!).text.toString())
            val height = Integer.parseInt(getHeightEditText(dialog!!).text.toString())

            setImageSize(width, height)
        } catch(e: NumberFormatException) {
            Toast.makeText(activity, getString(R.string.badNumberFormat), Toast.LENGTH_LONG).show()
        }
    }

    private fun setImageSize(width: Int, height: Int) {
        if(width < minDim || width > maxDim || height < minDim || height > maxDim) {
            Toast.makeText(activity, getString(R.string.dimensionsOutOfRange, minDim, maxDim), Toast.LENGTH_LONG).show()
            return
        }

        (activity as FractMainActivity).setImageSize(width, height)
    }

    companion object {
        const val widthKey = "width"
        const val heightKey = "height"

        fun newInstance(width: Int, height: Int): ImageSizeDialogFragment {
            val dialogFragment = ImageSizeDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putInt(widthKey, width)
                putInt(heightKey, height)
            }

            return dialogFragment
        }

        const val minDim = 1
        const val maxDim = 100000
    }
}