package at.searles.android.storage.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.android.storage.R
import at.searles.android.storage.StorageEditorCallback

class SaveAsDialogFragment: DialogFragment() {

    private lateinit var saveAsEditText: AutoCompleteTextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.storage_save_as_dialog, null)

        val oldNameKey = arguments!!.getString(oldNameKey)!!

        saveAsEditText = view.findViewById<AutoCompleteTextView>(R.id.saveAsEditText)!!.apply {
            setText(oldNameKey)
            selectAll()
            requestFocus()
        }

        val storageEditorCallback: StorageEditorCallback<*> = activity!! as StorageEditorCallback<*>
        val storageDataCache = storageEditorCallback.storageEditor.storageDataCache

        val contentAdapter = ItemsAdapter(context!!, storageDataCache)

        saveAsEditText.threshold = 1
        saveAsEditText.setAdapter(contentAdapter)

        return AlertDialog.Builder(activity!!)
            .setView(view)
            .setTitle(context!!.resources.getString(R.string.saveInXAs, storageEditorCallback.storageProvider.pathName))
            .setPositiveButton(android.R.string.ok) { _, _ -> run { saveAs(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)
            .create()
    }

    private fun saveAs() {
        val newName = saveAsEditText.text.toString()
        (activity as StorageEditorCallback<*>).storageEditor.saveAs(newName)
    }

    companion object {
        const val oldNameKey = "oldNameKey"

        fun newInstance(oldName: String): SaveAsDialogFragment {
            val args = Bundle()
            args.putString(oldNameKey, oldName)

            return SaveAsDialogFragment().apply {
                arguments = args
            }
        }
    }
}