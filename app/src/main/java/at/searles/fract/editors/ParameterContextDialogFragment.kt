package at.searles.fract.editors

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.commons.math.Cplx
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractlang.FractlangExpr
import at.searles.fractlang.nodes.CplxNode
import at.searles.fractlang.nodes.Node
import kotlinx.android.synthetic.main.reset_context_dialog.*

/**
 * Opens for now menu to reset a parameter.
 */
class ParameterContextDialogFragment: DialogFragment() {
    private lateinit var key: String
    private lateinit var setToCenterButton: Button
    private lateinit var centerOnValueButton: Button
    private lateinit var resetButton: Button

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = arguments!!.getString(nameKey)!!

        key = name

        val value = arguments!!.getString(valueKey)!!

        val cplxValue = try {
            // Try to parse value into a number.
            (FractlangExpr.fromString(value) as? CplxNode)?.value
        } catch(e: Exception) {
            // did not succeed. No big deal; it simply isn't a complex number.
            null
        }

        val view = LayoutInflater.from(context).inflate(R.layout.parameter_context_dialog, null)

        setToCenterButton = view.findViewById(R.id.setToCenterButton)
        centerOnValueButton = view.findViewById(R.id.centerOnValueButton)
        resetButton = view.findViewById(R.id.resetButton)

        if(cplxValue != null) {
            setToCenterButton.setOnClickListener {
                (activity as FractMainActivity).setParameterToCenter(key)
                dismiss()
            }

            centerOnValueButton.setOnClickListener {
                (activity as FractMainActivity).centerAt(cplxValue)
                dismiss()
            }
        } else {
            setToCenterButton.visibility = View.INVISIBLE
            centerOnValueButton.visibility = View.INVISIBLE
        }

        resetButton.setOnClickListener {
            (activity as FractMainActivity).resetParameter(key)
            dismiss()
        }

        return AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(resources.getString(R.string.editParameterName, name))
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)
            .create()
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