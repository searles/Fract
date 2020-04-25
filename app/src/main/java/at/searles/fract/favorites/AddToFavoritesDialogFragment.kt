package at.searles.fract.favorites

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.fract.FractMainActivity
import at.searles.fract.R

class AddToFavoritesDialogFragment: DialogFragment() {

    // TODO: Show current favorites in RecyclerView
    // TODO: Propose last name that was entered

    private lateinit var nameEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        val view = LayoutInflater.from(context).inflate(R.layout.add_to_favorites_dialog, null)

        nameEditText = view.findViewById<EditText>(R.id.nameEditText)!!
        nameEditText.requestFocus()

        builder
            .setView(view)
            .setTitle(R.string.addToFavorites)
            .setPositiveButton(android.R.string.ok) { _, _ -> run { addToFavorites(); dismiss() } }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        val nameEditText = dialog.findViewById<EditText>(R.id.nameEditText)!!
        nameEditText.requestFocus()

        return dialog
    }

    private fun addToFavorites() {
        val nameEditText = dialog!!.findViewById<EditText>(R.id.nameEditText)!!
        val name = nameEditText.text.toString()

        (activity as FractMainActivity).saveToFavorites(name)
    }

    companion object {
        fun newInstance(): AddToFavoritesDialogFragment {
            val dialogFragment = AddToFavoritesDialogFragment()
            dialogFragment.arguments = Bundle()
            return dialogFragment
        }
    }
}