package at.searles.fract.editors

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.FractSettings
import at.searles.fract.R

class SettingsDialogFragment: DialogFragment() {

    private lateinit var noneRadioButton: RadioButton
    private lateinit var scaleRadioButton: RadioButton
    private lateinit var confirmZoomCheckBox: CheckBox
    private lateinit var rotationLockCheckBox: CheckBox
    private lateinit var centerLockCheckBox: CheckBox
    private lateinit var lightRadioButton: RadioButton
    private lateinit var paletteRadioButton: RadioButton
    private lateinit var orbitRadioButton: RadioButton
    private lateinit var showGridCheckBox: CheckBox

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        @SuppressLint("InflateParams") // ok in this context
        val view = activity!!.layoutInflater.inflate(R.layout.settings_dialog, null)!!

        noneRadioButton = view.findViewById(R.id.noneRadioButton)!!
        scaleRadioButton = view.findViewById(R.id.scaleRadioButton)!!
        confirmZoomCheckBox = view.findViewById(R.id.confirmZoomCheckBox)!!
        rotationLockCheckBox = view.findViewById(R.id.rotationLockCheckBox)!!
        centerLockCheckBox = view.findViewById(R.id.centerLockCheckBox)!!
        showGridCheckBox = view.findViewById(R.id.showGridCheckBox)!!
        lightRadioButton = view.findViewById(R.id.lightRadioButton)!!
        paletteRadioButton = view.findViewById(R.id.paletteRadioButton)!!
        orbitRadioButton = view.findViewById(R.id.orbitRadioButton)!!

        initializeFields(savedInstanceState)

        builder
            .setView(view)
            .setTitle(R.string.settings)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { setPropertiesInActivity(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        return builder.show()
    }

    private fun initializeFields(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) {
            val settings = arguments!!.getParcelable<FractSettings>(settingsKey)!!

            noneRadioButton.isChecked = settings.mode == FractSettings.Mode.None
            scaleRadioButton.isChecked = settings.mode == FractSettings.Mode.Scale
            rotationLockCheckBox.isChecked = settings.isRotationLock
            centerLockCheckBox.isChecked = settings.isCenterLock
            confirmZoomCheckBox.isChecked = settings.isConfirmZoom
            lightRadioButton.isChecked = settings.mode == FractSettings.Mode.Light
            paletteRadioButton.isChecked = settings.mode == FractSettings.Mode.Palette
            orbitRadioButton.isChecked = settings.mode == FractSettings.Mode.Orbit
            showGridCheckBox.isChecked = settings.isGridEnabled
        }
    }

    private fun setPropertiesInActivity() {
        try {
            val mode = when {
                scaleRadioButton.isChecked -> FractSettings.Mode.Scale
                lightRadioButton.isChecked -> FractSettings.Mode.Light
                paletteRadioButton.isChecked -> FractSettings.Mode.Palette
                orbitRadioButton.isChecked -> FractSettings.Mode.Orbit
                else -> FractSettings.Mode.None
            }

            val isRotationLock = rotationLockCheckBox.isChecked
            val isCenterLock = centerLockCheckBox.isChecked
            val isConfirmZoom = confirmZoomCheckBox.isChecked
            val isGridEnabled = showGridCheckBox.isChecked

            val fractSettings = FractSettings(mode, isRotationLock, isCenterLock, isConfirmZoom, isGridEnabled)

            (activity as FractMainActivity).setSettings(fractSettings)
        } catch(e: NumberFormatException) {
            Toast.makeText(activity, getString(R.string.badNumberFormat), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val settingsKey = "settings"

        fun newInstance(settings: FractSettings): SettingsDialogFragment {
            val dialogFragment = SettingsDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putParcelable(settingsKey, settings)
            }

            return dialogFragment
        }
    }
}