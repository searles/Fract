package at.searles.fract.favorites

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.searles.android.storage.data.InvalidNameException
import at.searles.android.storage.data.PathContentProvider
import at.searles.android.storage.dialog.ReplaceExistingDialogFragment
import at.searles.fract.R
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractPropertiesAdapter
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*
import kotlin.math.min

class FavoritesProvider(private val context: Context): PathContentProvider(context.getDir(pathName, 0)) {
    // Cache images
    private val thumbnailCache = WeakHashMap<String, Bitmap>()

    override fun setImageInView(name: String, imageView: ImageView) {
        val thumbnail = loadThumbnail(name)
        imageView.setImageBitmap(thumbnail)

        // TODO Glide.with(context).load(custom).into(imageView)
    }

    private fun loadThumbnail(name: String): Bitmap? {
        return thumbnailCache.computeIfAbsent(name) {
            var thumbnail: Bitmap? = null

            load(name) { json ->
                thumbnail = loadThumbnailFromJsonString(json)
            }

            thumbnail
        }
    }

    private fun loadThumbnailFromJsonString(json: String): Bitmap? {
        return try {
            val obj = JSONObject(json)
            val base64String = obj.getString(thumbnailKey)
            base64ToBitmap(base64String)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayBitmapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream)
        return Base64.getEncoder().encodeToString(byteArrayBitmapStream.toByteArray())
    }

    private fun base64ToBitmap(base64String: String): Bitmap {
        val data = Base64.getDecoder().decode(base64String)
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }

    fun saveToFavorites(name: String, bitmapModel: FractBitmapModel, replaceExisting: Boolean) {
        // Save everything except for resolution
        val jsonProperties = FractPropertiesAdapter.toJson(bitmapModel.properties)

        val thumbnail = createThumbnailBitmap(bitmapModel.bitmap)
        jsonProperties.put(thumbnailKey, bitmapToBase64(thumbnail))

        val jsonString = jsonProperties.toString(4)

        val status: Boolean

        try {
            status = save(name, { jsonString }, replaceExisting)
        } catch (th: Throwable) {
            Toast.makeText(context, context.resources.getString(at.searles.android.storage.R.string.error, th.localizedMessage), Toast.LENGTH_LONG).show()
            th.printStackTrace()
            return
        }

        if(!status) {
            ReplaceExistingDialogFragment.create(name)
                .show((context as AppCompatActivity).supportFragmentManager, "dialog")
        }
    }

    /**
     * creates a new bitmap containing the center of the current image
     */
    private fun createThumbnailBitmap(original: Bitmap): Bitmap {
        val icon = Bitmap.createBitmap(thumbnailSize, thumbnailSize, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(icon)

        val w = original.width.toFloat()
        val h = original.height.toFloat()

        val scale = thumbnailSize.toFloat() / min(w, h)

        val transformation = Matrix().apply {
            setValues(
                floatArrayOf(
                    scale, 0f, (thumbnailSize - scale * w) * .5f, 0f, scale, (thumbnailSize - scale * h) * .5f, 0f, 0f, 1f
                )
            )
        }

        val paint = Paint().apply {
            isFilterBitmap = true
        }

        canvas.drawBitmap(original, transformation, paint)

        return icon
    }

    companion object {
        const val pathName = "favorites"
        const val thumbnailKey = "thumbnail"
        const val thumbnailSize = 96
    }
}