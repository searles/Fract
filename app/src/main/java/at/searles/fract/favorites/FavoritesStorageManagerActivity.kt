package at.searles.fract.favorites

import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageDataCache
import at.searles.fract.favorites.FavoritesStorageDataCache

class FavoritesStorageManagerActivity: StorageManagerActivity(pathName) {
    override fun createStorageDataCache(): StorageDataCache {
        return FavoritesStorageDataCache(this, storageProvider)
    }

    companion object {
        const val pathName = "favorites"
    }
}