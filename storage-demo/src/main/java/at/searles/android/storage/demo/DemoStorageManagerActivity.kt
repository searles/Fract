package at.searles.android.storage.demo

import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageDataCache

class DemoStorageManagerActivity: StorageManagerActivity("demo") {
    override fun createStorageDataCache(): StorageDataCache {
        return DemoStorageDataCache(storageProvider)
    }
}