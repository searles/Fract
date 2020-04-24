package at.searles.fract.experimental

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageSaver {
    fun saveImage(
        context: Context,
        bitmap: Bitmap,
        folderName: String,
        filename: String
    ): Uri? {
        if (android.os.Build.VERSION.SDK_INT < 29) {
            return savePre29(folderName, filename, bitmap, context)
        }

        val values = contentValues()
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + folderName)
        values.put(MediaStore.Images.Media.IS_PENDING, true)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)

        val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (uri != null) {
            saveImageToStream(bitmap, context.contentResolver.openOutputStream(uri))
            values.put(MediaStore.Images.Media.IS_PENDING, false)
            context.contentResolver.update(uri, values, null, null)

            return uri
        }

        return null
    }

    private fun savePre29(
        folderName: String,
        filename: String,
        bitmap: Bitmap,
        context: Context
    ): Uri? {
        val separator = "/"

        val directory =
            File(Environment.getExternalStorageDirectory().toString() + separator + folderName)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "$filename.png")
        saveImageToStream(bitmap, FileOutputStream(file))

        val values = contentValues()
        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    @SuppressLint("InlinedApi")
    private fun contentValues() : ContentValues {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        return values
    }

    private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
        if(outputStream != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        }
    }
}