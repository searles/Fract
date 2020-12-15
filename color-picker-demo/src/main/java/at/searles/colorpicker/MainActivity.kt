package at.searles.colorpicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import at.searles.colorpicker.dialog.ColorDialogCallback
import at.searles.colorpicker.dialog.ColorDialogFragment

class MainActivity : AppCompatActivity(), ColorDialogCallback {

    private var selectedColorPreview: ColorIconView? = null

    private var color: Int
        get() = selectedColorPreview!!.color
        set(value) { selectedColorPreview!!.color = value }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.color_view_layout)

        selectedColorPreview = findViewById(R.id.selectedColor)
    }

    fun onDialogStart(view: View) {
        when(view.id) {
            R.id.combinedButton -> {
                ColorDialogFragment.newInstance(color)
                    .show(supportFragmentManager, "dialog")
            }
        }
    }
    override fun setColor(dialogFragment: ColorDialogFragment, color: Int) {
        this.color = color
    }

}
