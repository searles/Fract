package at.searles.android.storage.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.android.storage.R
import at.searles.android.storage.StorageEditorCallback

/**
 * Parent activity must implement the can-save-
 */
class FinishWithoutSavingDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val isActivityCanceled = arguments!!.getBoolean(isActivityCanceledKey)

        val msg = if(isActivityCanceled) {
            context!!.resources.getString(R.string.cancelWithoutSavingQuestion)
        } else {
            context!!.resources.getString(R.string.returnWithoutSavingQuestion)
        }

        return AlertDialog.Builder(activity!!)
                .setTitle(R.string.unsavedChanges)
                .setMessage(msg)
                .setNegativeButton(android.R.string.no) { _, _ -> }
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    (activity as StorageEditorCallback<*>).storageEditor.finishWithoutSaving(isActivityCanceled)
                }
                .create()
    }

    companion object {
        private const val isActivityCanceledKey: String = "isActivityCanceled"

        fun newInstance(isActivityCanceled: Boolean): FinishWithoutSavingDialogFragment {
            val args = Bundle().apply {
                putBoolean(isActivityCanceledKey, isActivityCanceled)
            }

            return FinishWithoutSavingDialogFragment().apply {
                arguments = args
            }
        }
    }
}