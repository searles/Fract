package at.searles.fract.experimental

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.content.FileProvider
import at.searles.android.storage.data.PathContentProvider.Companion.FILE_PROVIDER
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.changes.NewFractPropertiesChange
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BulkCalculator(private val context: Context, propertiesWithFilename: Iterable<FractProperties>, private val model: FractBitmapModel): FractBitmapModel.Listener {

    private var iterator = propertiesWithFilename.iterator()

    private lateinit var currentProperties: FractProperties

    private var fileIndex = 0
    private val files = ArrayList<File>()

    fun start() {
        require(model.listener == null)
        startNextEntry()
    }

    private fun startNextEntry() {
        if(!iterator.hasNext()) {
            Log.i("BulkCalculator", "finished")

            // create zip file with all png files and share it.
            shareZip()
        }

        currentProperties = iterator.next()

        model.scheduleCalcPropertiesChange(NewFractPropertiesChange(currentProperties))
    }

    override fun started() {
        Log.i("BulkCalculator", "Processing File Nr $fileIndex...")
    }

    override fun setProgress(progress: Float) {
        // ignore
    }

    override fun bitmapUpdated() {
        // ignore
    }

    override fun finished() {
        Log.i("BulkCalculator", "Saving to file with index $fileIndex...")

        val outFile = File.createTempFile(
            "fract_$fileIndex",
            ".png",
            context.externalCacheDir
        )

        files.add(outFile)

        saveImage(FileOutputStream(outFile))

        fileIndex ++

        startNextEntry()
    }

    override fun propertiesChanged(src: FractBitmapModel) {
        // ignore
    }

    private fun saveImage(os: OutputStream) {
        os.use {
            if (!model.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {
                throw UnsupportedOperationException("compress not supported!")
            }
        }
    }

    private fun shareZip() {
        val outFile = File.createTempFile(
            "bulk_${System.currentTimeMillis()}",
            ".zip",
            context.externalCacheDir
        )

        var index = 0

        ZipOutputStream(FileOutputStream(outFile)).use { zipOut ->
            for(file in files) {
                Log.d("FilesProvider", "Putting $file into zip")
                zipOut.putNextEntry(ZipEntry("bulk_$index.png"))
                FileInputStream(file).use {
                    it.copyTo(zipOut)
                }
                zipOut.closeEntry()
                index++
            }
        }

        val contentUri = FileProvider.getUriForFile(context, FILE_PROVIDER, outFile)

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = "application/zip"
        }

        context.startActivity(Intent.createChooser(intent, "Store bulk"))
    }
}