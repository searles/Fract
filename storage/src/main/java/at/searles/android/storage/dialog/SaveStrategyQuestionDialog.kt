package at.searles.android.storage.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.android.storage.R
import at.searles.android.storage.StorageEditorCallback

/**
 * Parent activity must implement the can-save-
 */
class SaveStrategyQuestionDialog : DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = arguments!!.getString(nameKey)!!

        val view = LayoutInflater.from(context!!).inflate(R.layout.storage_save_strategy_question_dialog, null)

        val renameRadioButton = view.findViewById<RadioButton>(R.id.renameRadioButton)
        val overwriteRadioButton = view.findViewById<RadioButton>(R.id.overwriteRadioButton)

        val renamedName = (activity as StorageEditorCallback<*>).storageProvider.findNextAvailableName(name)

        renameRadioButton.text = activity!!.resources.getString(R.string.appendIndexToX, renamedName)

        return AlertDialog.Builder(context!!)
            .setView(view)
            .setCancelable(false)
            .setTitle(context!!.resources.getString(R.string.xAlreadyExistsInY, name, (activity as StorageEditorCallback<*>).storageProvider.pathName))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()

                val newName = when {
                    renameRadioButton.isChecked -> renamedName
                    overwriteRadioButton.isChecked -> name
                    else -> error("BUG, no selection")
                }

                (activity as StorageEditorCallback<*>).storageEditor.forceSaveAs(newName)
            }
            .show()
    }

    companion object {
        const val nameKey = "name"

        fun newInstance(newName: String): SaveStrategyQuestionDialog {
            val args = Bundle().apply {
                putString(nameKey, newName)
            }

            return SaveStrategyQuestionDialog().apply {
                arguments = args
            }
        }
    }
}