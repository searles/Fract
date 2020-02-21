package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import java.lang.NumberFormatException


class SettingsDialogFragment: DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.settings_dialog)
            .setTitle(R.string.settings)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { setPropertiesInActivity(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        // XXX This definitely could be improved by using a method like onPostCreateDialog if it exists.

        if(savedInstanceState == null) {
            val hasRotationLock = arguments!!.getBoolean(hasRotationLockKey)
            val isConfirmZoom = arguments!!.getBoolean(isConfirmZoomKey)
            val isTouchEnabled = arguments!!.getBoolean(isTouchEnabledKey)
            val isShowGrid = arguments!!.getBoolean(isShowGridKey)

            getTouchEnabledCheckBox(dialog).isChecked = isTouchEnabled
            getRotationLockCheckBox(dialog).isChecked = hasRotationLock
            getConfirmZoomCheckBox(dialog).isChecked = isConfirmZoom
            getShowGridCheckBox(dialog).isChecked = isShowGrid
        }


        return dialog
    }

    private fun getTouchEnabledCheckBox(dialog: Dialog) = dialog.findViewById<CheckBox>(R.id.touchEnabledCheckBox)!!
    private fun getConfirmZoomCheckBox(dialog: Dialog) = dialog.findViewById<CheckBox>(R.id.confirmZoomCheckBox)!!
    private fun getRotationLockCheckBox(dialog: Dialog) = dialog.findViewById<CheckBox>(R.id.rotationLockCheckBox)!!
    private fun getShowGridCheckBox(dialog: Dialog) = dialog.findViewById<CheckBox>(R.id.showGridCheckBox)!!

    private fun setPropertiesInActivity() {
        try {
            val isTouchEnabled = getTouchEnabledCheckBox(dialog!!).isChecked
            val hasRotationLock = getRotationLockCheckBox(dialog!!).isChecked 
            val isConfirmZoom = getConfirmZoomCheckBox(dialog!!).isChecked

            (activity as FractMainActivity).setSettings(isTouchEnabled, hasRotationLock, isConfirmZoom)
        } catch(e: NumberFormatException) {
            Toast.makeText(activity, getString(R.string.badNumberFormat), Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        const val isTouchEnabledKey = "isTouchEnabled"
        const val hasRotationLockKey = "hasRotationLock"
        const val isConfirmZoomKey = "isConfirmZoom"
        const val isShowGridKey = "isShowGrid"

        fun newInstance(isTouchEnabled: Boolean, hasRotationLock: Boolean, isConfirmZoom: Boolean, isShowGrid: Boolean): SettingsDialogFragment {
            val dialogFragment = SettingsDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putBoolean(isTouchEnabledKey, isTouchEnabled)
                putBoolean(hasRotationLockKey, hasRotationLock)
                putBoolean(isConfirmZoomKey, isConfirmZoom)
                putBoolean(isShowGridKey, isShowGrid)
            }

            return dialogFragment
        }
    }
}