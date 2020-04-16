package at.searles.fract.editors

import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import at.searles.commons.color.Palette
import at.searles.fract.FractMainActivity
import at.searles.fract.R
import at.searles.paletteeditor.PaletteAdapter

/**
 * Opens for now menu to reset a parameter.
 */
class PaletteContextDialogFragment: DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val label = arguments!!.getString(labelKey)!!

        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(R.layout.reset_context_dialog)
            .setTitle(resources.getString(R.string.editPalette))
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setCancelable(true)

        val dialog = builder.show()

        dialog.findViewById<Button>(R.id.resetButton)!!.setOnClickListener {
            (activity as FractMainActivity).resetPalette(label)
            dismiss()
        }

        return dialog
    }

    companion object {
        private const val paletteKey = "palette"
        private const val labelKey = "index"

        fun newInstance(label: String, palette: Palette): PaletteContextDialogFragment {
            val dialogFragment = PaletteContextDialogFragment()

            dialogFragment.arguments = Bundle().apply {
                putBundle(paletteKey, PaletteAdapter.toBundle(palette))
                putString(labelKey, label)
            }

            return dialogFragment
        }
    }
}