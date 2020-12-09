package at.searles.itemselector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import at.searles.itemselector.model.*

// TODO: Default

class FolderAdapter(private val context: Context, private val model: FolderModel) :
    ListAdapter<Entry, FolderAdapter.EntryViewHolder>(DiffCallback) {

    var listener: Listener? = null

    init {
        submitList(model.visibleEntries)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): EntryViewHolder {
        val layoutId = when(viewType) {
            itemViewType -> R.layout.item_layout
            closedFolderViewType -> R.layout.closed_folder_layout
            openFolderViewType -> R.layout.open_folder_layout
            else -> error("unexpected view type")
        }

        val view = LayoutInflater.from(context).inflate(layoutId, viewGroup, false)
        val viewHolder = EntryViewHolder(view)
        view.setOnClickListener(viewHolder)
        view.setOnLongClickListener(viewHolder)
        return viewHolder
    }

    override fun onBindViewHolder(entryViewHolder: EntryViewHolder, position: Int) {
        val name = getItem(position)
        entryViewHolder.bind(name)
    }

    override fun getItemViewType(position: Int): Int {
        val item = model.getItemAt(position)
        return if(item is FolderWithId) {
            if(item.isOpen) {
                openFolderViewType
            } else {
                closedFolderViewType
            }
        } else {
            itemViewType
        }
    }

    inner class EntryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val iconImageView: ImageView = itemView.findViewById(R.id.iconImageView)

        override fun onClick(view: View) {
            val entry = model.getItemAt(adapterPosition)

            if(entry is FolderWithId) {
                if(entry.isOpen) {
                    model.closeEntryAt(adapterPosition)
                    submitList(model.visibleEntries)
                } else {
                    model.openEntryAt(adapterPosition)
                    submitList(model.visibleEntries)
                }
            } else {
                val item = entry as ItemWithId
                listener?.itemClicked(item.parent, item)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val entry = model.getItemAt(adapterPosition)

            if(entry is FolderWithId) {
                return false
            }

            val item = entry as ItemWithId
            listener?.itemLongClicked(item.parent, item)
            return true
        }

        fun bind(item: Entry) {
            // set ui
            nameTextView.text = item.title
            descriptionTextView.text = item.description
            item.setImageInView(iconImageView)
        }

    }

    object DiffCallback: DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean {
            require(oldItem is HasId && newItem is HasId)
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean {
            require(oldItem is HasId && newItem is HasId)

            if(oldItem is FolderWithId && newItem is FolderWithId) {
                if(oldItem.isOpen != newItem.isOpen) {
                    return false
                }
            }

            return oldItem.id == newItem.id
        }
    }

    interface Listener {
        fun itemClicked(folder: Folder, item: Item)
        fun itemLongClicked(folder: Folder, item: Item)
    }

    companion object {
        const val itemViewType = 0
        const val closedFolderViewType = 1
        const val openFolderViewType = 2
    }
}