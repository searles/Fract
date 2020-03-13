package at.searles.fract.experimental

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.renderscript.RenderScript
import android.util.Log
import androidx.core.content.FileProvider
import at.searles.fract.FractMainActivity.Companion.FILE_PROVIDER
import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.changes.NewFractPropertiesChange
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BulkCalculator(private val context: Context, properties: Iterable<FractProperties>): FractBitmapModel.Listener {

    private var iterator = properties.iterator()

    private lateinit var currentProperties: FractProperties
    private var model: FractBitmapModel? = null

    private var fileIndex = 0
    private val files = ArrayList<File>()
    private val filenames = ArrayList<String>()

    fun start() {
        startNextEntry()
    }

    private fun startNextEntry() {
        if(!iterator.hasNext()) {
            Log.i("BulkCalculator", "finished")

            // create zip file with all png files and share it.
            shareZip()
        }

        currentProperties = iterator.next()

        if(model == null) {
            Log.i("BulkCalculator", "Creating model...")
            val rs = RenderScript.create(context)

            model = FractBitmapModel(
                rs,
                BitmapAllocation(rs, dim, dim),
                currentProperties
            ).apply {
                listener = this@BulkCalculator
                startTask()
            }
        } else {
            Log.i("BulkCalculator", "Scheduling next entry...")

            model!!.scheduleCalcPropertiesChange(NewFractPropertiesChange(currentProperties))
        }
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
        filenames.add(currentProperties.customParameters.getValue(filenameKey))

        saveImage(FileOutputStream(outFile))

        fileIndex ++

        startNextEntry()
    }

    override fun propertiesChanged(src: FractBitmapModel) {
        // ignore
    }

    private fun saveImage(os: OutputStream) {
        os.use {
            if (!model!!.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {
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
                zipOut.putNextEntry(ZipEntry("${filenames[index]}.png"))
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
            type = mimeType
        }

        context.startActivity(Intent.createChooser(intent, "Store bulk"))
    }

    companion object {
        const val filenameKey = "__filename"
        const val dim = 256
        const val mimeType = "application/zip"
    }
}