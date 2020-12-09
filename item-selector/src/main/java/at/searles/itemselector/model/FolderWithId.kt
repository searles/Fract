package at.searles.itemselector.model

class FolderWithId(private val folder: Folder,
                   override val id: Int,
                   val isOpen: Boolean = false): HasId, Folder by folder {

    fun open(): FolderWithId {
        return FolderWithId(folder, id, true)
    }

    fun close(): FolderWithId {
        return FolderWithId(folder, id, false)
    }

    override val children: List<ItemWithId> = folder.children.mapIndexed { index, item ->
        ItemWithId(item, this, id + index + 1)
    }
}