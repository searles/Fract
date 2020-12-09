package at.searles.android.storage.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import at.searles.android.storage.R
import at.searles.android.storage.StorageManagerActivity
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ImportFragment: Fragment() {
    private var zipInputStream: ZipInputStream? = null
    private var zipEntry: ZipEntry? = null
    private var importedList = ArrayList<String>()

    private lateinit var provider: StorageProvider
    private var strategy = Strategy.AlwaysAsk

    private var questionDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        provider = (context as StorageManagerActivity).storageProvider

        if(zipInputStream == null) {
            val uri: Uri = arguments!!.getParcelable(uriKey)!!
            val inputStream = context.contentResolver.openInputStream(uri)!!

            try {
                zipInputStream = ZipInputStream(inputStream)
            } catch(e: Exception) {
                Toast.makeText(context, context.resources.getString(R.string.errorWithMsg, e.message), Toast.LENGTH_LONG).show()
                finishImport()
                return
            }
        }

        doImport()
    }

    override fun onDetach() {
        if(questionDialog != null) {
            questionDialog!!.dismiss()
        }
        super.onDetach()
    }

    override fun onDestroy() {
        try {
            zipInputStream?.close()
        } catch (e: java.lang.Exception) {
            Log.i(javaClass.simpleName, "Could not close zip input stream: $e")
            e.printStackTrace()
        } finally {
            super.onDestroy()
        }
    }

    private fun doImport() {
        while(importZipEntry()) {
            // empty body
        }
    }

    private fun finishImport() {
        (context as StorageManagerActivity).finishImport(importedList)
    }

    /**
     * return false if the import should be interrupted for whatever reason.
     */
    private fun importZipEntry(): Boolean {
        require(zipInputStream != null)

        if(zipEntry == null) {
            zipEntry = zipInputStream!!.nextEntry
        }

        if(zipEntry == null) {
            finishImport()
            return false
        }

        val name = provider.decode(zipEntry!!.name)

        if(provider.exists(name)) {
            return when(strategy) {
                Strategy.AlwaysAsk -> { createQuestionDialog(name); false }
                Strategy.OverwriteAll -> { forceImportZipEntry(name); true }
                Strategy.RenameAll -> { forceImportZipEntry(provider.findNextAvailableName(name)); true }
                Strategy.SkipAll -> { advanceToNextZipEntry(); true }
            }
        }

        forceImportZipEntry(name)
        return true
    }

    private fun advanceToNextZipEntry() {
        zipInputStream!!.closeEntry()
        zipEntry = null
    }

    private fun forceImportZipEntry(name: String) {
        try {
            val value = provider.load(object: InputStream() {
                override fun read(): Int {
                    return zipInputStream!!.read()
                }

                override fun close() {
                    // do nothing.
                }
            })

            provider.save(name, value)
            importedList.add(name)
        } catch(e: Exception) {
            Log.i(javaClass.simpleName, "Could not read ${zipEntry!!.name}")
            e.printStackTrace()
        }

        advanceToNextZipEntry()
    }

    @SuppressLint("InflateParams")
    private fun createQuestionDialog(name: String) {
        val view = LayoutInflater.from(context!!).inflate(R.layout.storage_import_question_dialog, null)

        val renameRadioButton = view.findViewById<RadioButton>(R.id.renameRadioButton)
        val skipRadioButton = view.findViewById<RadioButton>(R.id.skipRadioButton)
        val overwriteRadioButton = view.findViewById<RadioButton>(R.id.overwriteRadioButton)
        val applyToAllCheckBox = view.findViewById<CheckBox>(R.id.applyToAllCheckBox)

        renameRadioButton.text = context!!.resources.getString(R.string.appendIndexName, provider.findNextAvailableName(name))

        fun applyStrategy() {
            if(applyToAllCheckBox.isChecked) {
                strategy = when {
                    renameRadioButton.isChecked -> {
                        Strategy.RenameAll
                    }
                    overwriteRadioButton.isChecked -> {
                        Strategy.OverwriteAll
                    }
                    skipRadioButton.isChecked -> {
                        Strategy.SkipAll
                    }
                    else -> {
                        Strategy.AlwaysAsk
                    }
                }
            } else {
                when {
                    renameRadioButton.isChecked -> {
                        forceImportZipEntry(provider.findNextAvailableName(name))
                    }
                    overwriteRadioButton.isChecked -> {
                        forceImportZipEntry(name)
                    }
                    else -> {
                        // skip.
                        advanceToNextZipEntry()
                    }
                }
            }

            doImport()
        }

        questionDialog = AlertDialog.Builder(context!!)
            .setView(view)
            .setCancelable(false)
            .setTitle(context!!.resources.getString(R.string.xAlreadyExistsInY, name, provider.pathName))
            .setNegativeButton(R.string.cancel) { dialog, _ ->
                dialog.dismiss()
                questionDialog = null
                finishImport()
            }
            .setPositiveButton(R.string.ok) { dialog, _ ->
                dialog.dismiss()
                questionDialog = null
                applyStrategy()
            }
            .show()
    }

    enum class Strategy { AlwaysAsk, OverwriteAll, RenameAll, SkipAll }

    companion object {
        private const val uriKey = "uri"

        fun newInstance(uri: Uri): ImportFragment {
            val args = Bundle().apply {
                putParcelable(uriKey, uri)
            }

            return ImportFragment().apply {
                arguments = args
            }
        }
    }
}