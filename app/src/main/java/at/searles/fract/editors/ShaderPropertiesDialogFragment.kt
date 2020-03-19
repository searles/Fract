package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fractbitmapmodel.ShaderProperties
import com.google.android.material.textfield.TextInputLayout
import java.lang.NumberFormatException

class ShaderPropertiesDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.shader_properties_dialog)
            .setTitle(R.string.setShaderProperties)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { setShaderProperties(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        val checkBox = dialog.findViewById<CheckBox>(R.id.useShaderCheckBox)!!

        if(savedInstanceState == null) {
            checkBox.isChecked = arguments!!.getBoolean(useLightEffectKey)

            dialog.findViewById<EditText>(R.id.polarEditText)!!.setText(arguments!!.getDouble(polarKey).toString())
            dialog.findViewById<EditText>(R.id.azimuthEditText)!!.setText(arguments!!.getDouble(azimuthKey).toString())
            dialog.findViewById<EditText>(R.id.ambientReflectionEditText)!!.setText(arguments!!.getDouble(ambientReflectionKey).toString())
            dialog.findViewById<EditText>(R.id.diffuseReflectionEditText)!!.setText(arguments!!.getDouble(diffuseReflectionKey).toString())
            dialog.findViewById<EditText>(R.id.specularReflectionEditText)!!.setText(arguments!!.getDouble(specularReflectionKey).toString())
            dialog.findViewById<EditText>(R.id.shininessEditText)!!.setText(arguments!!.getInt(shininessKey).toString())
        }

        checkBox.setOnCheckedChangeListener { _, isChecked -> updateLightEffectsEnabledStatus(isChecked, this.dialog!!) }
        updateLightEffectsEnabledStatus(checkBox.isChecked, dialog)

        return dialog
    }

    private fun updateLightEffectsEnabledStatus(isChecked: Boolean, dialog: Dialog) {
        val visibility = if(isChecked) View.VISIBLE else View.INVISIBLE

        dialog.findViewById<TextInputLayout>(R.id.polarInputLayout)!!.visibility = visibility
        dialog.findViewById<TextInputLayout>(R.id.azimuthInputLayout)!!.visibility = visibility
        dialog.findViewById<TextInputLayout>(R.id.ambientReflectionInputLayout)!!.visibility = visibility
        dialog.findViewById<TextInputLayout>(R.id.diffuseReflectionInputLayout)!!.visibility = visibility
        dialog.findViewById<TextInputLayout>(R.id.specularReflectionInputLayout)!!.visibility = visibility
        dialog.findViewById<TextInputLayout>(R.id.shininessInputLayout)!!.visibility = visibility
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

    private fun setShaderProperties() {
        val useLightEffect = dialog!!.findViewById<CheckBox>(R.id.useShaderCheckBox)!!.isChecked

        val polarStr = dialog!!.findViewById<EditText>(R.id.polarEditText)!!.text.toString()
        val azimuthStr = dialog!!.findViewById<EditText>(R.id.azimuthEditText)!!.text.toString()
        val ambientStr = dialog!!.findViewById<EditText>(R.id.ambientReflectionEditText)!!.text.toString()
        val diffuseStr = dialog!!.findViewById<EditText>(R.id.diffuseReflectionEditText)!!.text.toString()
        val specularStr = dialog!!.findViewById<EditText>(R.id.specularReflectionEditText)!!.text.toString()
        val shininessStr = dialog!!.findViewById<EditText>(R.id.shininessEditText)!!.text.toString()

        try {
            val polar = strToDouble(polarStr)
            val azimuth = strToDouble(azimuthStr)
            val ambient = strToDouble(ambientStr)
            val diffuse = strToDouble(diffuseStr)
            val specular = strToDouble(specularStr)
            val shininess = strToDouble(shininessStr).toInt()

            val shaderProperties = ShaderProperties(useLightEffect, polar, azimuth, ambient.toFloat(), diffuse.toFloat(), specular.toFloat(), shininess)

            (activity as FractMainActivity).setShaderProperties(shaderProperties)
        } catch(e: NumberFormatException) {
            Toast.makeText(activity, getString(R.string.badNumberFormat), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun newInstance(shaderProperties: ShaderProperties): ShaderPropertiesDialogFragment {
            val dialogFragment = ShaderPropertiesDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putBoolean(useLightEffectKey, shaderProperties.useLightEffect)
                putDouble(polarKey, shaderProperties.polarAngle)
                putDouble(azimuthKey, shaderProperties.azimuthAngle)
                putDouble(ambientReflectionKey, shaderProperties.ambientReflection.toDouble())
                putDouble(diffuseReflectionKey, shaderProperties.diffuseReflection.toDouble())
                putDouble(specularReflectionKey, shaderProperties.specularReflection.toDouble())
                putInt(shininessKey, shaderProperties.shininess)
            }

            return dialogFragment
        }

        const val useLightEffectKey = "useLightEffect"
        const val polarKey = "polar"
        const val azimuthKey = "azimuth"
        const val ambientReflectionKey = "ambientReflection"
        const val diffuseReflectionKey = "diffuseReflection"
        const val specularReflectionKey = "specularReflection"
        const val shininessKey = "shininess"
    }
}