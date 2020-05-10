package at.searles.fract.favorites

import android.content.Context
import android.content.Intent
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider

class FavoritesStorageEditor(private val context: Context, callback: StorageEditorCallback<FavoriteEntry>, provider: StorageProvider) : StorageEditor<FavoriteEntry>(provider, callback, FavoritesStorageManagerActivity::class.java) {
    override fun createReturnIntent(target: Intent, name: String?, value: FavoriteEntry): Intent {
        error("not implemented")
    }

    override fun serialize(value: FavoriteEntry): String {
        return value.serialize()
    }

    override fun deserialize(serializedValue: String): FavoriteEntry {
        return FavoriteEntry.deserialize(serializedValue)
    }

    override fun createStorageDataCache(provider: StorageProvider): StorageDataCache {
        return FavoritesStorageDataCache(context, provider)
    }
}
