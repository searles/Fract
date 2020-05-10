//package at.searles.fract.favorites
//
//import android.content.Context
//import android.graphics.*
//import android.util.Log
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import at.searles.android.storage.data.PathContentProvider
//import at.searles.android.storage.dialog.ReplaceExistingDialogFragment
//import at.searles.fractbitmapmodel.FractBitmapModel
//import at.searles.fractbitmapmodel.FractPropertiesAdapter
//import org.json.JSONObject
//import java.io.ByteArrayOutputStream
//import java.util.*
//import kotlin.math.min
//
//class FavoritesProvider(private val context: Context): PathContentProvider(context.getDir(pathName, 0)) {
//    // Cache images
//    private val thumbnailCache = WeakHashMap<String, Bitmap>()
//
//    override fun setImageInView(name: String, imageView: ImageView) {
//        try {
//            val thumbnail = loadThumbnail(name)
//            imageView.setImageBitmap(thumbnail)
//        } catch(e: Exception) {
//            Log.i("FavoritesProvider", "Error setting thumbnail of image: $name. Maybe the file was just deleted.")
//            e.printStackTrace()
//        }
//
//        // TODO Glide.with(context).load(custom).into(imageView)
//    }
//
//    private fun loadThumbnail(name: String): Bitmap? {
//        return thumbnailCache.computeIfAbsent(name) {
//            var thumbnail: Bitmap? = null
//
//            load(name) { json ->
//                thumbnail = loadThumbnailFromJsonString(json)
//            }
//
//            thumbnail
//        }
//    }
//
//
//
//    fun saveToFavorites(name: String, bitmapModel: FractBitmapModel, replaceExisting: Boolean) {
//        // Save everything except for resolution
//        val jsonProperties = FractPropertiesAdapter.toJson(bitmapModel.properties)
//
//        val thumbnail = createThumbnailBitmap(bitmapModel.bitmap)
//        jsonProperties.put(thumbnailKey, bitmapToBase64(thumbnail))
//
//        val jsonString = jsonProperties.toString(4)
//
//        val status: Boolean
//
//        try {
//            status = save(name, { jsonString }, replaceExisting)
//        } catch (th: Throwable) {
//            Toast.makeText(context, context.resources.getString(at.searles.android.storage.R.string.error, th.localizedMessage), Toast.LENGTH_LONG).show()
//            th.printStackTrace()
//            return
//        }
//
//        if(!status) {
//            ReplaceExistingDialogFragment.create(name)
//                .show((context as AppCompatActivity).supportFragmentManager, "dialog")
//        }
//    }
//
//
//    companion object {
//        const val pathName = "favorites"
//        const val thumbnailKey = "thumbnail"
//        const val thumbnailSize = 96
//    }
//}