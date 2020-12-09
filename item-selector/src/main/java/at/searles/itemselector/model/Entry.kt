package at.searles.itemselector.model

import android.widget.ImageView

interface Entry {
    val key: String
    val title: String
    val description: String
    fun setImageInView(imageView: ImageView)
}