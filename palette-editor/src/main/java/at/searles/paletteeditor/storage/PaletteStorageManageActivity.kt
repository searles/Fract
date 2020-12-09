package at.searles.paletteeditor.storage

import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageDataCache
import at.searles.paletteeditor.PaletteEditorActivity

class PaletteStorageManageActivity: StorageManagerActivity(PaletteEditorActivity.directoryName) {
    override fun createStorageDataCache(): StorageDataCache {
        return PaletteStorageDataCache(storageProvider, this)
    }
}
