package at.searles.fract.favorites

import android.widget.ImageView
import at.searles.android.storage.data.FilesProvider
import java.io.File

class FavoritesItem: FilesProvider(File("favorites")) {
    override fun setImageInView(name: String, imageView: ImageView) {
        // load icon.
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


}