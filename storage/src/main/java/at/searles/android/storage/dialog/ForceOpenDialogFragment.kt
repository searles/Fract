package at.searles.android.storage.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.android.storage.R
import at.searles.android.storage.StorageEditorCallback

class ForceOpenDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = arguments?.getString(nameKey)!!

        return AlertDialog.Builder(activity!!)
                .setTitle(R.string.unsavedChanges)
                .setMessage(context!!.resources.getString(R.string.discardChangesQuestion, name))
                .setNegativeButton(android.R.string.no) { _, _ -> }
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    (activity as StorageEditorCallback<*>).storageEditor.forceOpen(name)
                }
                .create()
    }

    companion object {
        const val nameKey = "name"

        fun newInstance(name: String): ForceOpenDialogFragment {
            val args = Bundle()
            args.putString(nameKey, name)

            return ForceOpenDialogFragment().apply { arguments = args }
        }
    }
}