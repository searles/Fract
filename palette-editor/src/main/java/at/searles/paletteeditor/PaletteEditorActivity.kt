package at.searles.paletteeditor

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageProvider
import at.searles.colorpicker.dialog.ColorDialogCallback
import at.searles.colorpicker.dialog.ColorDialogFragment
import at.searles.commons.color.Palette
import at.searles.multiscrollview.CompositionCrossPane
import at.searles.multiscrollview.InnerPaneView
import at.searles.multiscrollview.MultiScrollView
import at.searles.paletteeditor.colorsview.ColorsAdapter
import at.searles.paletteeditor.paletteeditorview.*
import at.searles.paletteeditor.storage.PaletteStorageEditor

class PaletteEditorActivity : StorageEditorCallback<Palette>, AppCompatActivity(), ColorDialogCallback {

    private val colorsView by lazy {
        findViewById<RecyclerView>(R.id.colorsView)
    }

    private val multiScrollView by lazy {
        findViewById<MultiScrollView>(R.id.multiScrollView)
    }

    private val innerPaneView by lazy {
        multiScrollView.findViewById<InnerPaneView>(R.id.innerPaneView)
    }

    private val toolbar: Toolbar by lazy {
        findViewById<Toolbar>(R.id.toolbar)
    }

    private lateinit var saveMenuItem: MenuItem

    private lateinit var model: PaletteEditorModel
    private lateinit var palettePane: PaletteEditorPane
    private lateinit var controller: PaletteEditorController

    override lateinit var storageEditor: StorageEditor<Palette>
    override lateinit var storageProvider: StorageProvider

    override var value: Palette
        get() = model.createPalette()
        set(value) {
            model.restoreFromPalette(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.palette_editor_activity_main)
        setSupportActionBar(toolbar)

        val paletteBundle = if (savedInstanceState != null) {
            savedInstanceState.getBundle(paletteKey)!!
        } else {
            intent.getBundleExtra(paletteKey)
        }

        storageProvider = StorageProvider(directoryName, this)
        storageEditor = PaletteStorageEditor(this, storageProvider, this)
        storageEditor.onRestoreInstanceState(savedInstanceState)

        initializePaletteModel(PaletteAdapter.toPalette(paletteBundle))
        initializeController()
        initializePaletteEditor()
        initializeColorsView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(paletteKey, PaletteAdapter.toBundle(model.createPalette()))
        storageEditor.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.palette_editor_main_menu, menu)

        saveMenuItem = menu.findItem(R.id.saveAction)
        storageEditor.fireStorageItemStatus()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.openStorageAction -> {
                storageEditor.onOpen(openRequestCode)
                true
            }
            R.id.returnAction -> {
                storageEditor.onFinish(false)
                true
            }
            R.id.saveAction -> {
                storageEditor.onSave()
                true
            }
            R.id.saveAsAction -> {
                storageEditor.onSaveAs()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeController() {
        controller = PaletteEditorController(this, model)
    }

    private fun initializeColorsView() {
        val orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayout.HORIZONTAL
        } else {
            LinearLayout.VERTICAL
        }

        colorsView.layoutManager = LinearLayoutManager(this, orientation, false)
        colorsView.adapter = ColorsAdapter(
            this
        )
    }

    private fun initializePaletteModel(palette: Palette?) {
        model = PaletteEditorModel()

        if(palette != null) {
            model.restoreFromPalette(palette)
        }

        model.addListener(object: PaletteEditorModel.Listener {
            override fun onPaletteSizeChanged() {
                multiScrollView.updateSize()
                innerPaneView.invalidate()
                storageEditor.notifyValueModified()
            }

            override fun onOffsetChanged() {
                innerPaneView.invalidate()
                storageEditor.notifyValueModified()
            }

            override fun onColorsChanged() {
                innerPaneView.invalidate()
                storageEditor.notifyValueModified()
            }

            override fun onSelectionChanged() {
                innerPaneView.invalidate()
            }
        })
    }

    private fun initializePaletteEditor() {
        palettePane = PaletteEditorPane(innerPaneView, model).apply { listener = controller }

        val hOffsetPane = HorizontalOffsetPane(innerPaneView, palettePane).apply { listener = controller }

        val vOffsetPane = VerticalOffsetPane(innerPaneView, palettePane).apply { listener = controller }

        val hControlPane = HorizontalEditTablePane(innerPaneView, palettePane).apply { listener = controller }

        val vControlPane = VerticalEditTablePane(innerPaneView, palettePane).apply { listener = controller }

        innerPaneView.innerPane = CompositionCrossPane(vOffsetPane, hOffsetPane, vControlPane, hControlPane, palettePane)
    }

    override fun setColor(dialogFragment: ColorDialogFragment, color: Int) {
        controller.setColor(dialogFragment, color)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == openRequestCode) {
            storageEditor.invalidate()
            if (resultCode == Activity.RESULT_OK) {
                val name = data!!.getStringExtra(StorageManagerActivity.nameKey)!!
                storageEditor.open(name)
            }
        }
    }

    override fun onStorageItemChanged(name: String?, isModified: Boolean) {
        toolbar.subtitle = if(name != null)
            if(isModified)
                "*$name"
            else
                name
        else
            getString(R.string.untitled)

        saveMenuItem.isEnabled = isModified && name != null
    }

    companion object {
        private const val openRequestCode = 3593
        const val paletteKey = "palette"
        const val directoryName = "palettes"
    }
}
