package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R

/**
 * Opens for now menu to reset a parameter.
 */
class ParameterContextDialogFragment: DialogFragment() {
    private lateinit var key: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        val name = arguments!!.getString(nameKey)!!

        key = name

        builder
            .setView(R.layout.parameter_context_dialog)
            .setTitle(resources.getString(R.string.editParameterName, name))
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        dialog.findViewById<Button>(R.id.resetButton)!!.setOnClickListener {
            (activity as FractMainActivity).resetParameter(key)
            dismiss()
        }

        dialog.findViewById<Button>(R.id.setToCenterButton)!!.setOnClickListener {
            (activity as FractMainActivity).setParameterToCenter(key)
            dismiss()
        }

        /*
        val value = arguments!!.getString(valueKey)!!
         */

        return dialog
    }

    companion object {
        private const val nameKey = "name"
        private const val valueKey = "value"

        fun newInstance(name: String, value: String): ParameterContextDialogFragment {
            val dialogFragment = ParameterContextDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putString(nameKey, name)
                putString(valueKey, value)
            }

            return dialogFragment
        }
    }
}