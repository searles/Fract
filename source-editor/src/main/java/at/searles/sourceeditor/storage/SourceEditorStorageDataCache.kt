package at.searles.sourceeditor.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.text.format.DateFormat
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider
import at.searles.sourceeditor.R
import java.util.*


class SourceEditorStorageDataCache(private val context: Context, private val storageProvider: StorageProvider): StorageDataCache(storageProvider) {

    private val dateFormat = DateFormat.getDateFormat(context)

    override fun loadBitmap(name: String): Bitmap {
        val icon = context.resources.getDrawable(R.drawable.ic_text, context.theme)

        val bitmap = Bitmap.createBitmap(icon.intrinsicWidth, icon.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        icon.setBounds(0, 0, canvas.width, canvas.height)
        icon.draw(canvas)

        return bitmap
    }

    override fun loadDescription(name: String): String {
        val file = storageProvider.findPathEntry(name) ?: return ""
        return context.getString(R.string.lastModifiedOnX, dateFormat.format(Date(file.lastModified())))
    }
}