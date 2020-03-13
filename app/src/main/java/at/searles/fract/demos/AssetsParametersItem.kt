package at.searles.fract.demos

import android.widget.ImageView
import at.searles.commons.math.Scale
import at.searles.itemselector.model.Item
import com.bumptech.glide.Glide

class AssetsParametersItem(sourceKey: String, override val key: String, override val title: String, override val description: String,
                           val scale: Scale?, val parameters: Map<String, String>) : Item {

    private val iconFilename = "$sourceKey-$key.png"

    override fun setImageInView(imageView: ImageView) {
        Glide
            .with(imageView.context)
            .load(DemosFolderHolder.getIconUri(iconFilename))
            .centerCrop()
            .into(imageView)
    }
}