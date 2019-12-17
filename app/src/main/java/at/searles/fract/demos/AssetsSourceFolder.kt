package at.searles.fract.demos

import android.net.Uri
import android.widget.ImageView
import at.searles.itemselector.model.Folder
import com.bumptech.glide.Glide

class AssetsSourceFolder(override val name: String, override val description: String, val iconFilename: String, val sourceFilename: String, parameters: List<AssetsParametersItem>) : Folder {
    override val children: List<AssetsParametersItem> = parameters

    override fun setImageInView(imageView: ImageView) {
        Glide
            .with(imageView.context)
            .load(Uri.parse("file:///android_asset/icons/$iconFilename"))
            .centerCrop()
            // TODO .placeholder(R.drawable.loading_spinner)
            .into(imageView)
    }
}