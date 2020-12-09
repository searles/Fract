package at.searles.android.storage.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class Exporter(private val context: Context, private val provider: StorageProvider) {
    private fun writeZipToOs(names: List<String>, outputStream: OutputStream) {
        ZipOutputStream(outputStream).use { zipOut ->
            for(name in names) {
                zipOut.putNextEntry(ZipEntry(provider.encode(name)))

                provider.save(object: OutputStream() {
                    override fun write(b: Int) {
                        zipOut.write(b)
                    }

                    override fun close() {
                        // do nothing.
                    }
                }, provider.load(name))
                zipOut.closeEntry()
            }
        }
    }

    fun share(names: List<String>): Intent {

        val outFile = File.createTempFile(
            "${provider.pathName}_${System.currentTimeMillis()}",
            ".zip",
            context.externalCacheDir
        )

        writeZipToOs(names, FileOutputStream(outFile))

        val contentUri = FileProvider.getUriForFile(context,
            StorageProvider.FILE_PROVIDER, outFile)

        return Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type =
                StorageProvider.mimeType
        }
    }

    fun export(names: List<String>, uri: Uri) {
        val outputStream = context.contentResolver.openOutputStream(uri)!!
        writeZipToOs(names, outputStream)
    }
}