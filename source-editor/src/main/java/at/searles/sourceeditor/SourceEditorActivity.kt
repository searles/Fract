package at.searles.sourceeditor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageProvider
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.extensions.FractlangObserver
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.format.CodeFormatter
import at.searles.sourceeditor.storage.SourceEditorStorageEditor

// XXX Next developments:
// * Undo/Redo
// * Simulate/Execute for point x:y
class SourceEditorActivity : StorageEditorCallback<String>, AppCompatActivity() {

    private val sourceCodeEditor: EditText by lazy {
        findViewById<EditText>(R.id.sourceEditText)
    }

    private lateinit var resetParametersMenuItem: MenuItem

    private val selectedParameters
        get() = if(!resetParametersMenuItem.isChecked) parameters else emptyMap()

    override var value: String
        get() = sourceCodeEditor.text.toString()
        set(value) { sourceCodeEditor.setText(value)}

    private lateinit var parameters: Map<String, String>
    private lateinit var syntaxUpdater: DelayedUpdater

    private lateinit var saveMenuItem: MenuItem

    private val toolbar: Toolbar by lazy {
        findViewById<Toolbar>(R.id.toolbar)
    }

    override lateinit var storageProvider: StorageProvider
    override lateinit var storageEditor: StorageEditor<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.source_editor_activity_main)
        setSupportActionBar(toolbar)

        if(savedInstanceState == null) {
            // fetch from intent
            value = intent.getStringExtra(sourceKey)!!
        }

        storageProvider = StorageProvider(directoryName, this)
        storageEditor = SourceEditorStorageEditor(this, storageProvider, this)
        storageEditor.onRestoreInstanceState(savedInstanceState)

        parameters = toStringMap(intent.getBundleExtra(parametersKey)!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        storageEditor.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        syntaxUpdater.cancel()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()

        // Set up Syntax Highlighting
        val textWatcher = ChangesTextWatcher()

        val updateTask = SourceHighlightUpdateTask(
                sourceCodeEditor,
                textWatcher,
                FractlangObserver(sourceCodeEditor.resources),
                FractlangGrammar.program,
                FractlangGrammar.eof)

        syntaxUpdater = DelayedUpdater(updateTask, 500).apply { tick() }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.source_editor_main_menu, menu)

        resetParametersMenuItem = menu.findItem(R.id.resetParameters)

        if(parameters.isEmpty()) {
            resetParametersMenuItem.isEnabled = false
        }

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
            R.id.format -> {
                formatCode()
                true
            }
            R.id.compile -> {
                tryCompile()
                true
            }
            R.id.returnAction -> {
                if(tryCompile()) {
                    storageEditor.onFinish(false)
                }

                true
            }
            R.id.resetParameters -> {
                resetParametersMenuItem.isChecked = !resetParametersMenuItem.isChecked
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if(requestCode == openRequestCode) {
            if(resultCode == Activity.RESULT_OK) {
                val name = intent!!.getStringExtra(StorageManagerActivity.nameKey)!!
                storageEditor.open(name)
            }
        }
    }

    private fun formatCode() {
        CodeFormatter(FractlangGrammar.program, FractlangGrammar.whiteSpaceId).format(EditTextAdapter(sourceCodeEditor.editableText))
    }

    private fun tryCompile(): Boolean {
        try {
            FractlangProgram(value, selectedParameters)
            Toast.makeText(this, getString(R.string.successfullyCompiled), Toast.LENGTH_SHORT).show()
            return true
// TODO        } catch(e: ParserLookaheadException) {
//            sourceCodeEditor.setSelection(e.unexpectedTokenStart.toInt(), e.unexpectedTokenEnd.toInt())
//            FractlangObserver(resources).onParserError(sourceCodeEditor.editableText, e)
//            Toast.makeText(this, "Unexpected token. Expected ${e.failedParser().right()}", Toast.LENGTH_LONG).show()
//            e.printStackTrace()
        } catch(e: SemanticAnalysisException) {
            sourceCodeEditor.setSelection(e.trace.start.toInt(), e.trace.end.toInt())
            FractlangObserver(resources).error(sourceCodeEditor.editableText, e.trace.start, e.trace.end)
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        return false
    }

    inner class ChangesTextWatcher: TextWatcher {
        override fun afterTextChanged(editable: Editable) {
            syntaxUpdater.tick()
            storageEditor.notifyValueModified()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    companion object {
        const val directoryName = "dev"
        const val sourceKey = "source"
        const val parametersKey = "parameters"

        private const val openRequestCode = 4792

        fun toBundle(map: Map<String, String>): Bundle {
            val bundle = Bundle()
            map.forEach { (key, value) -> bundle.putString(key, value) }
            return bundle
        }

        fun toStringMap(bundle: Bundle): Map<String, String> {
            return bundle.keySet().map { key -> key to bundle.getString(key)!! }.toMap()
        }
    }
}
