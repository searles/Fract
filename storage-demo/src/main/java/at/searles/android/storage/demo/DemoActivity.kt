package at.searles.android.storage.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageProvider
import at.searles.storage.R

class DemoActivity : StorageEditorCallback<String>, AppCompatActivity() {

    private val contentEditText: EditText by lazy {
        findViewById<EditText>(R.id.contentEditText)
    }

    private val toolbar: Toolbar by lazy {
        findViewById<Toolbar>(R.id.toolbar)
    }

    private lateinit var saveMenuItem: MenuItem

    override lateinit var storageEditor: StorageEditor<String>

    override lateinit var storageProvider: StorageProvider

    override var value: String
        get() = contentEditText.text.toString()
        set(value) {
            contentEditText.setText(value)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo)

        setSupportActionBar(toolbar)

        toolbar.subtitle = "Subtitle"
        toolbar.setNavigationIcon(R.drawable.ic_edit_24dp)

        storageProvider = StorageProvider("demo", this)

        storageEditor = DemoStorageEditor(this, storageProvider)
        storageEditor.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        storageEditor.onSaveInstanceState(outState)
    }

    override fun onStorageItemChanged(name: String?, isModified: Boolean) {
        toolbar.subtitle = if(isModified && name != null) {
            "*$name"
        } else {
            name ?: "(unknown)"
        }

        saveMenuItem.isEnabled = isModified && name != null
    }

    override fun onResume() {
        super.onResume()

        contentEditText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                storageEditor.notifyValueModified()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.demo_main_menu, menu)
        saveMenuItem = menu.findItem(R.id.save)

        storageEditor.fireStorageItemStatus()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.open -> {
                storageEditor.onOpen(openRequestCode)
                true
            }
            R.id.save -> {
                storageEditor.onSave()
                true
            }
            R.id.saveAs -> {
                storageEditor.onSaveAs()
                true
            }
            R.id.finish -> {
                storageEditor.onFinish(false)
            }
            R.id.cancel -> {
                storageEditor.onFinish(true)
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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

    companion object {
        private const val openRequestCode = 1234
    }

}
