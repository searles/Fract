package at.searles.itemselector.model

interface Folder: Entry {
    val children: List<Item> // must not be empty
}