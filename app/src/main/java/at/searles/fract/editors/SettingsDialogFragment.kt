package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.FractSettings
import at.searles.fract.R
import kotlinx.android.synthetic.main.settings_dialog.*


class SettingsDialogFragment: DialogFragment() {

    private lateinit var touchEnabledCheckBox: CheckBox
    private lateinit var confirmZoomCheckBox: CheckBox
    private lateinit var rotationLockCheckBox: CheckBox
    private lateinit var showGridCheckBox: CheckBox
    private lateinit var editLightsOnScreenCheckBox: CheckBox

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.settings_dialog)
            .setTitle(R.string.settings)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { setPropertiesInActivity(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        touchEnabledCheckBox = dialog.findViewById(R.id.touchEnabledCheckBox)!!
        confirmZoomCheckBox = dialog.findViewById(R.id.confirmZoomCheckBox)!!
        rotationLockCheckBox = dialog.findViewById(R.id.rotationLockCheckBox)!!
        showGridCheckBox = dialog.findViewById(R.id.showGridCheckBox)!!
        editLightsOnScreenCheckBox = dialog.findViewById(R.id.editLightsOnScreenCheckBox)!!

        initializeFields(savedInstanceState)

        return dialog
    }

    private fun initializeFields(savedInstanceState: Bundle?) {
        if(savedInstanceState == null) {
            val settings = arguments!!.getParcelable<FractSettings>(settingsKey)!!

            touchEnabledCheckBox.isChecked = settings.isTouchEnabled
            rotationLockCheckBox.isChecked = settings.isRotationLock
            confirmZoomCheckBox.isChecked = settings.isConfirmZoom
            showGridCheckBox.isChecked = settings.isGridEnabled
            editLightsOnScreenCheckBox.isChecked = settings.isEditLightsOnScreenEnabled
        }
    }

    private fun setPropertiesInActivity() {
        try {
            val isTouchEnabled = touchEnabledCheckBox.isChecked
            val isRotationLock = rotationLockCheckBox.isChecked
            val isConfirmZoom = confirmZoomCheckBox.isChecked
            val isGridEnabled = showGridCheckBox.isChecked
            val isEditLightsOnScreen = editLightsOnScreenCheckBox.isChecked

            val fractSettings = FractSettings(isRotationLock, isTouchEnabled, isConfirmZoom, isGridEnabled, isEditLightsOnScreen)

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