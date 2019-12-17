package at.searles.fract.demos

import android.widget.ImageView
import at.searles.itemselector.model.Item
import com.bumptech.glide.Glide

class AssetsParametersItem(override val name: String, override val description: String, private val iconFilename: String, private val parameters: Map<String, String>) : Item {
    override fun setImageInView(imageView: ImageView) {
        Glide
            .with(imageView.context)
            .load(DemosFoldersHolder.getIconUri(iconFilename))
            .centerCrop()
            // TODO .placeholder(R.drawable.loading_spinner)
            .into(imageView)
    }
}