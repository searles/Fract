package at.searles.fract.favorites

import android.content.Context
import android.widget.ImageView
import at.searles.android.storage.data.PathContentProvider

class FavoritesProvider(context: Context): PathContentProvider(context.getDir(path, 0)) {
    override fun setImageInView(name: String, imageView: ImageView) {
        // FIXME load icon.
    }

    companion object {
        const val path = "favorites"
    }
}