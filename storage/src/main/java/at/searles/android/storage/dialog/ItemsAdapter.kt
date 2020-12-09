package at.searles.android.storage.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import at.searles.android.storage.R
import at.searles.android.storage.data.StorageDataCache

class ItemsAdapter(context: Context, private val storageDataCache: StorageDataCache): ArrayAdapter<String>(context, R.layout.storage_item_layout) {

    init {
        addAll(storageDataCache.getNames())
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.storage_item_layout, parent, false)

        val textView = view.findViewById<TextView>(R.id.textView)
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        val name = getItem(position)!!

        textView.text = name
        imageView.setImageBitmap(storageDataCache.getBitmap(name))

        return view
    }
}