package at.searles.android.storage.demo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider

class DemoStorageDataCache(storageProvider: StorageProvider) : StorageDataCache(storageProvider) {
    override fun loadBitmap(name: String): Bitmap {
        val bitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawRect(RectF(0f, 0f, 64f, 64f), Paint().apply { color = 0xff00ff00.toInt(); style = Paint.Style.FILL })
        canvas.drawText(name, 0f, 32f, Paint().apply { color = 0xffff0000.toInt(); style = Paint.Style.FILL })

        return bitmap
    }

    override fun loadDescription(name: String): String {
        return "Hi, $name!"
    }
}