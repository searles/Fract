package at.searles.fract.favorites

import android.graphics.*
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.FractPropertiesAdapter
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.min

class FavoriteEntry(val properties: FractProperties, val icon: Bitmap) {

    fun serialize(): String {
        // Save everything except for resolution
        val jsonProperties = FractPropertiesAdapter.toJson(properties)

        jsonProperties.put(thumbnailKey, bitmapToBase64(icon))

        return jsonProperties.toString(4)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayBitmapStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayBitmapStream)
        return Base64.getEncoder().encodeToString(byteArrayBitmapStream.toByteArray())
    }

    companion object {
        private const val thumbnailSize = 96
        const val thumbnailKey = "thumbnail"

        fun create(bitmapModel: FractBitmapModel): FavoriteEntry {
            val icon = createThumbnailBitmap(bitmapModel.bitmap)
            val properties = bitmapModel.properties

            return FavoriteEntry(properties, icon)
        }

        fun deserialize(serialized: String): FavoriteEntry {
            val json = JSONObject(serialized)

            val properties = FractPropertiesAdapter.fromJson(json)
            val icon = base64ToBitmap(json.getString(thumbnailKey))

            return FavoriteEntry(properties, icon)
        }

        fun loadThumbnailFromJsonString(json: String): Bitmap? {
            return try {
                val obj = JSONObject(json)
                val base64String = obj.getString(thumbnailKey)
                base64ToBitmap(base64String)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        private fun base64ToBitmap(base64String: String): Bitmap {
            val data = Base64.getDecoder().decode(base64String)
            return BitmapFactory.decodeByteArray(data, 0, data.size)
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
    }
}