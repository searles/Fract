package at.searles.itemselector.model

class ItemWithId(private val item: Item, val parent: Folder, override val id: Int): HasId, Item by item