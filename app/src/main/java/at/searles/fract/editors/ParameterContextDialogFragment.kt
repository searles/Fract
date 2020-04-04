package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractlang.FractlangExpr
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.parsing.FractlangParser

/**
 * Opens for now menu to reset a parameter.
 */
class ParameterContextDialogFragment: DialogFragment() {
    private lateinit var key: String
    private var expr: Node? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        val name = arguments!!.getString(nameKey)!!

        key = name

        val value = arguments!!.getString(valueKey)!!

        try {
            // Try to parse value into a number.
            expr = FractlangExpr.fromString(value)
        } catch(e: Exception) {
            // did not succeed. No big deal.
        }

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

// TODO: based on value        dialog.findViewById<Button>(R.id.setToCenterButton)!!.setOnClickListener {
//            (activity as FractMainActivity).setParameterToCenter(key)
//            dismiss()
//        }

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