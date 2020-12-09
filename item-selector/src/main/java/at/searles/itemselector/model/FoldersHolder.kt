package at.searles.itemselector.model

/**
 * Must contain a constructor that has one single argument: The current context.
 */
interface FoldersHolder {
    val folders: List<Folder>
}