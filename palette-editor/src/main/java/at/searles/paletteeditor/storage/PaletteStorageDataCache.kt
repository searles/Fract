package at.searles.paletteeditor.storage

import android.content.Context
import android.graphics.Bitmap
import android.text.format.DateFormat
import android.util.Log
import androidx.core.graphics.set
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider
import at.searles.paletteeditor.PaletteAdapter
import org.json.JSONObject
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class PaletteStorageDataCache(private val storageProvider: StorageProvider, context: Context): StorageDataCache(storageProvider) {

    private val dateFormat = DateFormat.getDateFormat(context)

    override fun loadBitmap(name: String): Bitmap {
        return try {
            val palette = PaletteAdapter.toPalette(JSONObject(storageProvider.load(name)))
            val bm = Bitmap.createBitmap(palette.width, palette.height, Bitmap.Config.ARGB_8888)

            repeat(palette.height) { y ->
                repeat(palette.width) { x ->
                    bm[x, y] = palette.colorTable[y][x].toRgb().toArgb()
                }
            }

            bm
        } catch(e: Exception) {
            Log.i(javaClass.simpleName, "Could not load $name")
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }

    override fun loadDescription(name: String): String {
        val file = storageProvider.findPathEntry(name) ?: return ""
        return "last modified: ${dateFormat.format(Date(file.lastModified()))}"
    }
}