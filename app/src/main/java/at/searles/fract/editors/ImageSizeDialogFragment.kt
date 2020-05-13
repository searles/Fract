package at.searles.fract.editors

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R


class ImageSizeDialogFragment: DialogFragment() {

    private lateinit var resolutionSpinner: Spinner
    private lateinit var widthEditText: EditText
    private lateinit var heightEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.image_size_dialog, null)

        resolutionSpinner = view.findViewById(R.id.resolutionSpinner)
        widthEditText = view.findViewById(R.id.widthEditText)
        heightEditText = view.findViewById(R.id.heightEditText)

        resolutionSpinner.onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // ignore
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when(resolutionSpinner.adapter.getItem(position)) {
                        "720p" -> {
                            widthEditText.setText(1280.toString())
                            heightEditText.setText(720.toString())
                            widthEditText.isEnabled = false
                            heightEditText.isEnabled = false
                        }
                        "1080p" -> {
                            widthEditText.setText(1920.toString())
                            heightEditText.setText(1080.toString())
                            widthEditText.isEnabled = false
                            heightEditText.isEnabled = false
                        }
                        "1440p" -> {
                            widthEditText.setText(2560.toString())
                            heightEditText.setText(1440.toString())
                            widthEditText.isEnabled = false
                            heightEditText.isEnabled = false
                        }
                        else -> {
                            widthEditText.isEnabled = true
                            heightEditText.isEnabled = true
                        }
                    }
                }
            }

        if(savedInstanceState == null) {
            val width = arguments!!.getInt(widthKey)
            val height = arguments!!.getInt(heightKey)

            widthEditText.setText("$width")
            heightEditText.setText("$height")

            resolutionSpinner.setSelection(positionCustomEntry)
        }

        builder
            .setView(view)
            .setTitle(R.string.setImageSize)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { validateImageSize(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        return builder.show()
    }

    private fun validateImageSize() {
        try {
            val width = Integer.parseInt(widthEditText.text.toString())
            val height = Integer.parseInt(heightEditText.text.toString())

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

        const val positionCustomEntry = 3
    }
}