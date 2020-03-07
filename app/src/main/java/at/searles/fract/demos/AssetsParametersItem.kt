package at.searles.fract.demos

import android.widget.ImageView
import at.searles.commons.math.Scale
import at.searles.itemselector.model.Item
import com.bumptech.glide.Glide

class AssetsParametersItem(override val key: String, override val title: String, override val description: String,
                           private val iconFilename: String,
                           val tags: Set<String>,
                           val scale: Scale?,
                           val parameters: Map<String, String>) : Item {
    override fun setImageInView(imageView: ImageView) {
        Glide
            .with(imageView.context)
            .load(DemosFolderHolder.getIconUri(iconFilename))
            .centerCrop()
            .into(imageView)
    }
}