package at.searles.fract.demos

import android.net.Uri
import android.widget.ImageView
import at.searles.itemselector.model.Folder
import com.bumptech.glide.Glide

class AssetsSourceFolder(override val key: String, override val title: String, override val description: String, private val iconFilename: String, val sourceFilename: String, parameterSets: List<AssetsParametersItem>) : Folder {
    override val children: List<AssetsParametersItem> = parameterSets

    override fun setImageInView(imageView: ImageView) {
        Glide
            .with(imageView.context)
            .load(Uri.parse("file:///android_asset/icons/$iconFilename"))
            .centerCrop()
            .into(imageView)
    }
}