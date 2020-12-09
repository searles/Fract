package at.searles.itemselector.model

class FolderModel(folders: List<Folder>) {
    var visibleEntries: List<Entry> = folders.fold(emptyList<FolderWithId>()) {
                list, folder ->
                list + FolderWithId(folder,
                    if(list.isEmpty()) 0 else (list.last().children.last().id + 1))
        }
        private set

    fun openEntryAt(position: Int) {
        val entry= visibleEntries[position]

        require(entry is FolderWithId && !entry.isOpen)

        val openedEntry = entry.open()

        visibleEntries =
            visibleEntries.take(position) +
                    openedEntry +
                    entry.children +
                    visibleEntries.takeLast(visibleEntries.size - (position + 1))
    }

    fun closeEntryAt(position: Int) {
        val entry= visibleEntries[position]
        require(entry is FolderWithId && entry.isOpen)

        val closedEntry = entry.close()

        visibleEntries =
            visibleEntries.take(position) +
                    closedEntry +
                    visibleEntries.takeLast(visibleEntries.size - (position + 1) - entry.children.size)
    }


    fun getItemAt(position: Int): Entry {
        return visibleEntries[position]
    }
}
