package at.searles.android.storage

import at.searles.android.storage.data.StorageProvider

interface StorageEditorCallback<A> {
    /**
     * Used to get or set the current value of an editor.
     */
    var value: A
    fun onStorageItemChanged(name: String?, isModified: Boolean)
    val storageEditor: StorageEditor<A>
    val storageProvider: StorageProvider
}