package at.searles.fract.editors

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
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

    private lateinit var resetButton: Button
    private lateinit var orthognoalizeButton: Button

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.reset_context_dialog, null)

        resetButton = view.findViewById(R.id.resetButton)
        orthognoalizeButton = view.findViewById(R.id.orthogonalizeButton)

        resetButton.setOnClickListener {
            (activity as FractMainActivity).resetScale()
            dismiss()
        }

        orthognoalizeButton.setOnClickListener {
            (activity as FractMainActivity).orthogonalizeScale()
            dismiss()
        }

        return AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(resources.getString(R.string.editScale))
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)
            .create()
    }

    companion object {
        private const val scaleKey = "scale"

        fun newInstance(scale: Scale): ScaleContextDialogFragment {
            val dialogFragment = ScaleContextDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putDoubleArray(scaleKey, FractPropertiesAdapter.scaleToArray(scale))
            }

            return dialogFragment
        }
    }
}