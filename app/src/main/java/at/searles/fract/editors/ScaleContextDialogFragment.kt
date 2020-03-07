package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.commons.math.Scale
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractbitmapmodel.FractPropertiesAdapter

/**
 * Opens for now menu to reset a parameter.
 */
class ScaleContextDialogFragment: DialogFragment() {
    private lateinit var key: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.parameter_context_dialog)
            .setTitle(resources.getString(R.string.editScale))
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        dialog.findViewById<Button>(R.id.resetButton)!!.setOnClickListener {
            (activity as FractMainActivity).resetScale()
            dismiss()
        }

        return dialog
    }

    companion object {
        private const val scaleKey = "scale"

        fun newInstance(scale: Scale): ScaleDialogFragment {
            val dialogFragment = ScaleDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putDoubleArray(scaleKey, FractPropertiesAdapter.scaleToArray(scale))
            }

            return dialogFragment
        }
    }
}