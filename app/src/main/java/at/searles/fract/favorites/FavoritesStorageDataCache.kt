package at.searles.fract.favorites

import android.content.Context
import android.graphics.*
import android.text.format.DateFormat
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider
import at.searles.fract.R
import java.util.*

class FavoritesStorageDataCache(private val context: Context, private val storageProvider: StorageProvider) : StorageDataCache(storageProvider) {
    private val dateFormat = DateFormat.getDateFormat(context)

    override fun loadBitmap(name: String): Bitmap {
        val content = storageProvider.load(name)
        return FavoriteEntry.loadThumbnailFromJsonString(content) ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
    }

    override fun loadDescription(name: String): String {
        val file = storageProvider.findPathEntry(name) ?: return ""
        return context.getString(R.string.lastModified, dateFormat.format(Date(file.lastModified())))
    }
}