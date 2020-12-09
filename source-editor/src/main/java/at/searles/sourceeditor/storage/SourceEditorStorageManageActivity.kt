package at.searles.sourceeditor.storage

import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageDataCache
import at.searles.sourceeditor.SourceEditorActivity

class SourceEditorStorageManageActivity: StorageManagerActivity(SourceEditorActivity.directoryName) {
    override fun createStorageDataCache(): StorageDataCache {
        return SourceEditorStorageDataCache(this, storageProvider)
    }
}
