package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.commons.math.Scale
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractbitmapmodel.ShaderProperties
import java.lang.NumberFormatException

class ShaderPropertiesDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.shader_properties_dialog)
            .setTitle(R.string.setShaderProperties)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { setScale(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        if(savedInstanceState == null) {
            dialog.findViewById<EditText>(R.id.polarEditText)!!.setText(arguments!!.getDouble("a").toString())
            dialog.findViewById<EditText>(R.id.azimuthEditText)!!.setText(arguments!!.getDouble("b").toString())
            dialog.findViewById<EditText>(R.id.ambientReflectionEditText)!!.setText(arguments!!.getDouble("c").toString())
            dialog.findViewById<EditText>(R.id.diffuseReflectionEditText)!!.setText(arguments!!.getDouble("d").toString())
            dialog.findViewById<EditText>(R.id.specularReflectionEditText)!!.setText(arguments!!.getDouble("e").toString())
            dialog.findViewById<EditText>(R.id.shininessEditText)!!.setText(arguments!!.getInt("f").toString())
        }

        return dialog
    }

    /**
     * Convenience to convert empty strings to 0.
     */
    private fun strToDouble(s: String): Double {
        if(s.isEmpty()) {
            return 0.0
        }

        return s.toDouble()
    }

    private fun setScale() {
        val aString = dialog!!.findViewById<EditText>(R.id.aEditText)!!.text.toString()
        val bString = dialog!!.findViewById<EditText>(R.id.bEditText)!!.text.toString()
        val cString = dialog!!.findViewById<EditText>(R.id.cEditText)!!.text.toString()
        val dString = dialog!!.findViewById<EditText>(R.id.dEditText)!!.text.toString()
        val eString = dialog!!.findViewById<EditText>(R.id.eEditText)!!.text.toString()
        val fString = dialog!!.findViewById<EditText>(R.id.fEditText)!!.text.toString()

        try {
            val a = strToDouble(aString)
            val b = strToDouble(bString)
            val c = strToDouble(cString)
            val d = strToDouble(dString)
            val e = strToDouble(eString)
            val f = strToDouble(fString)

            if(a * d - b * c == 0.0) {
                Toast.makeText(activity, getString(R.string.zeroDeterminante), Toast.LENGTH_LONG).show()
                return
            }

            val scale = Scale(a, b, c, d, e, f)

            (activity as FractMainActivity).setScale(scale)
        } catch(e: NumberFormatException) {
            Toast.makeText(activity, getString(R.string.badNumberFormat), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun newInstance(shaderProperties: ShaderProperties): ShaderPropertiesDialogFragment {
            val dialogFragment = ShaderPropertiesDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putDouble("a", shaderProperties.polarAngle)
                putDouble("b", shaderProperties.azimuthAngle)
//                putDouble("c", shaderProperties.ambientReflection)
//                putDouble("d", shaderProperties.diffuseReflection)
//                putDouble("e", shaderProperties.specularReflection)
//                putDouble("f", shaderProperties.shininess)
            }

            return dialogFragment
        }

        const val useLightEffectKey = "polar"
//        const val polarKey = "polar"
//        const val polarKey = "polar"
//        const val polarKey = "polar"
//        const val polarKey = "polar"
//        const val polarKey = "polar"
//        const val polarKey = "polar"
    }
}