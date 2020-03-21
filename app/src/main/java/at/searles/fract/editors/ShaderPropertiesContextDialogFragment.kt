package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractbitmapmodel.ShaderProperties

/**
 * Opens for now menu to reset a parameter.
 */
class ShaderPropertiesContextDialogFragment: DialogFragment() {
    private lateinit var key: String

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.reset_context_dialog)
            .setTitle(resources.getString(R.string.editShaderProperties))
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        dialog.findViewById<Button>(R.id.resetButton)!!.setOnClickListener {
            (activity as FractMainActivity).resetShaderProperties()
            dismiss()
        }

        return dialog
    }

    companion object {
        private const val shaderPropertiesKey = "shaderProperties"

        fun newInstance(shaderProperties: ShaderProperties): ShaderPropertiesContextDialogFragment {
            val dialogFragment = ShaderPropertiesContextDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putBundle(shaderPropertiesKey, shaderProperties.toBundle())
            }

            return dialogFragment
        }
    }
}