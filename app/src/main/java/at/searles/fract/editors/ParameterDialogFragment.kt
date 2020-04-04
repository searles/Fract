package at.searles.fract.editors

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import com.google.android.material.textfield.TextInputLayout


class ParameterDialogFragment: DialogFragment() {

    private lateinit var key: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        val name = arguments!!.getString(nameKey)!!

        key = name

        builder
            .setView(R.layout.parameter_edit_dialog)
            .setTitle(resources.getString(R.string.editParameterName, name))
            .setPositiveButton(android.R.string.ok) { _, _ -> run {} } // will be modified later
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        if(savedInstanceState == null) {
            val value = arguments!!.getString(valueKey)!!

            getValueEditText(dialog).setText(value)
        }

        val okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)

        okButton.setOnClickListener {
            try {
                setParameter()
                dismiss()
            } catch(e: SemanticAnalysisException) {
                setErrorMessage(e.message)
            }
        }

        return dialog
    }

    private fun getValueEditText(dialog: Dialog) = dialog.findViewById<EditText>(R.id.valueEditText)!!

    private fun setErrorMessage(msg: String?) {
        val inputLayout = dialog!!.findViewById<TextInputLayout>(R.id.parameterInputLayout)
        inputLayout.error = "Error: $msg"
    }

    private fun setParameter() {
        val value = getValueEditText(dialog!!).text.toString()
        (activity as FractMainActivity).setParameter(key, value)
    }

    companion object {
        private const val nameKey = "name"
        private const val valueKey = "value"

        fun newInstance(name: String, value: String): ParameterDialogFragment {
            val dialogFragment = ParameterDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putString(nameKey, name)
                putString(valueKey, value)
            }

            return dialogFragment
        }
    }
}