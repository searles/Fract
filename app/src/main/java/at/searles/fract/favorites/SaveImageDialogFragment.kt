package at.searles.fract.favorites

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.fract.experimental.ImageSaver

class SaveImageDialogFragment: DialogFragment() {

    private lateinit var nameEditText: EditText
    private lateinit var addToFavoritesCheckBox: CheckBox

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.save_image_dialog, null)

        nameEditText = view.findViewById<EditText>(R.id.nameEditText)!!
        nameEditText.requestFocus()

        addToFavoritesCheckBox = view.findViewById<CheckBox>(R.id.addToFavoritesCheckBox)!!

        builder
            .setView(view)
            .setTitle(R.string.saveImage)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { saveImage(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        return builder.show()
    }

    private fun saveImage() {
        val name = nameEditText.text.toString()

        if(name.isEmpty()) {
            Toast.makeText(context, context!!.resources.getText(R.string.invalidName), Toast.LENGTH_LONG).show()
            return
        }

        val bitmap = (activity as FractMainActivity).getBitmap()

        try {
            val uri = ImageSaver.saveImage(context!!, bitmap, "Fract", name)

            if(uri == null) {
                Toast.makeText(context, getString(R.string.contentProviderCouldNotSaveImage), Toast.LENGTH_LONG).show()
                return
            }

            Toast.makeText(context, getString(R.string.successfullySavedImage, uri.toString()), Toast.LENGTH_LONG).show()

            if(addToFavoritesCheckBox.isChecked) {
                addToFavorites(name)
            }
        } catch(e: Exception) {
            Toast.makeText(context, context!!.resources.getText(R.string.error, e.localizedMessage), Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun addToFavorites(name: String) {
        (activity as FractMainActivity).saveToFavorites(name)
    }

    companion object {
        fun newInstance(): SaveImageDialogFragment {
            // TODO Propose a name.

            val dialogFragment = SaveImageDialogFragment()
            dialogFragment.arguments = Bundle()
            return dialogFragment
        }
    }
}