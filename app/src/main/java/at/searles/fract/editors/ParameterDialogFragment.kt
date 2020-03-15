package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException


class ParameterDialogFragment: DialogFragment() {

    private lateinit var key: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        val name = arguments!!.getString(nameKey)!!

        key = name

        builder
            .setView(R.layout.parameter_edit_dialog)
            .setTitle(resources.getString(R.string.editParameterName, name))
            .setPositiveButton(android.R.string.ok) { _, _ -> run { setParameter(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        if(savedInstanceState == null) {
            val value = arguments!!.getString(valueKey)!!

            getValueEditText(dialog).setText(value)
        }

        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun getValueEditText(dialog: Dialog) = dialog.findViewById<EditText>(R.id.valueEditText)!!

    private fun setParameter() {
        try {
            val value = getValueEditText(dialog!!).text.toString()
            (activity as FractMainActivity).setParameter(key, value)
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(activity, getString(R.string.compileError, e.localizedMessage), Toast.LENGTH_LONG).show()
        }
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