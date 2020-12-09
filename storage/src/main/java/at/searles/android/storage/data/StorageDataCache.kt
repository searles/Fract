package at.searles.android.storage.data

import android.graphics.Bitmap
import java.util.*

abstract class StorageDataCache(private val provider: StorageProvider) {

    private val names = ArrayList<String>()
    private val bitmapCache = WeakHashMap<String, Bitmap>()
    private val descriptionCache = WeakHashMap<String, String>()

    init {
        invalidate()
    }

    fun getNames(): List<String> {
        return names
    }

    fun getBitmap(name: String): Bitmap {
        return bitmapCache.getOrPut(name, { loadBitmap(name) })
    }

    fun getDescription(name: String): String {
        return descriptionCache.getOrPut(name, { loadDescription(name) })
    }

    fun invalidate() {
        names.clear()
        names.addAll(provider.names)
        bitmapCache.clear()
        descriptionCache.clear()
    }

    abstract fun loadBitmap(name: String): Bitmap
    abstract fun loadDescription(name: String): String
}