package at.searles.paletteeditor.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import at.searles.commons.color.Lab
import at.searles.commons.color.Palette
import at.searles.commons.color.Rgb
import at.searles.commons.util.IntIntMap
import at.searles.paletteeditor.PaletteAdapter
import at.searles.paletteeditor.PaletteEditorActivity

class DemoActivity : AppCompatActivity() {

    private val runButton: Button by lazy {
        findViewById<Button>(R.id.button)
    }

    private lateinit var palette: Palette

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        palette = Palette(3, 2, 0.6f, 0.3f, IntIntMap<Lab>().apply {
            this[0, 0] = Rgb(1f, 0f, 0f).toLab()
            this[1, 1] = Rgb(0f, 1f, 0f).toLab()
            this[2, 0] = Rgb(0f, 0f, 1f).toLab()
        })

        runButton.setOnClickListener {
            Intent(this, PaletteEditorActivity::class.java).also {
                it.putExtra(PaletteEditorActivity.paletteKey, PaletteAdapter.toBundle(palette))
                startActivityForResult(it, paletteRequestCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == paletteRequestCode) {
            if(resultCode == Activity.RESULT_OK) {
                palette = PaletteAdapter.toPalette(data!!.getBundleExtra(paletteKey)!!)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val paletteRequestCode = 111
        const val paletteKey = PaletteEditorActivity.paletteKey
    }
}
