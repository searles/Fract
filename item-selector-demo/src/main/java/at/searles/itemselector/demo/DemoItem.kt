package at.searles.itemselector.demo

import android.widget.ImageView
import at.searles.itemselector.model.Item

class DemoItem(override val key: String): Item {
    override val title: String = "title: $key sdf sdf sdf sdf sf sf sfd sdf sf sf sdf s df sdf" +
            " sf sd fs df sf s df sdf sdf s df"
    override val description: String = "description: $key sdfs dfas f asf saf asfdasdfas fasf as fasf" +
            " saf saf as f asf as fas dfasfd sadfasf saf"
    override fun setImageInView(imageView: ImageView) {
        // ignore
    }

    override fun toString(): String {
        return title
    }
}