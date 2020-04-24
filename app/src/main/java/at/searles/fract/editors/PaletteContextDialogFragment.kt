package at.searles.fract.editors

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
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

        @SuppressLint("InflateParams")
        val view = LayoutInflater.from(context).inflate(R.layout.palette_context_dialog, null)

        view.findViewById<Button>(R.id.resetButton)!!.setOnClickListener {
            (activity as FractMainActivity).resetPalette(label)
            dismiss()
        }

        val excludePaletteModeCheckBox = view.findViewById<CheckBox>(R.id.noPaletteModeCheckBox)!!
        excludePaletteModeCheckBox.isChecked = (activity as FractMainActivity).getSettings().excludeFromPaletteMode.contains(label)

        excludePaletteModeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            val oldSettings = (activity as FractMainActivity).getSettings()
            (activity as FractMainActivity).setSettings(
                oldSettings.isExcludeFromPaletteMode(label, isChecked)
            )
        }

        val builder = AlertDialog.Builder(activity!!)

        builder
            .setView(view)
            .setTitle(resources.getString(R.string.editPalette))
            .setNeutralButton(R.string.close) { _, _ -> dismiss() }
            .setCancelable(true)

        return builder.show()
    }

    companion object {
        private const val paletteKey = "palette"
        private const val labelKey = "label"

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