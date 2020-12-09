package at.searles.android.storage.dialog

import android.annotation.SuppressLint
import android.os.Bundle
import android.app.Dialog
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.appcompat.app.AlertDialog
import at.searles.android.storage.R
import at.searles.android.storage.StorageManagerActivity
import java.lang.IllegalArgumentException

/**
 * This is only called from StorageManageActivity
 */
class RenameDialogFragment: DialogFragment() {

    private lateinit var renameEditText: AutoCompleteTextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val storageDataCache = (activity!! as StorageManagerActivity).createStorageDataCache()
        val contentAdapter = ItemsAdapter(context!!, storageDataCache)

        val oldNameKey = arguments!!.getString(oldNameKey)!!

        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context!!).inflate(R.layout.storage_rename_dialog, null)

        renameEditText = view.findViewById<AutoCompleteTextView>(R.id.renameEditText)!!.apply {
            threshold = 1
            setAdapter(contentAdapter)
            setText(oldNameKey)
            selectAll()
            requestFocus()
        }

        return AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(R.string.rename)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { rename(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)
            .show()
    }

    private fun rename() {
        val renameEditText = dialog!!.findViewById<EditText>(R.id.renameEditText)

        val oldName = arguments!!.getString(oldNameKey)?:throw IllegalArgumentException()
        val newName = renameEditText.text.toString()

        (activity as StorageManagerActivity).rename(oldName, newName)
    }

    companion object {
        const val oldNameKey = "oldNameKey"

        fun newInstance(oldName: String): RenameDialogFragment {
            val dialogFragment = RenameDialogFragment()
            dialogFragment.arguments = Bundle().also { it.putString(oldNameKey, oldName) }
            return dialogFragment
        }
    }
}