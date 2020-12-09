package at.searles.android.storage.demo

import android.content.Intent
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider

class DemoStorageEditor(callback: StorageEditorCallback<String>, provider: StorageProvider) : StorageEditor<String>(provider, callback, DemoStorageManagerActivity::class.java) {
    override fun createReturnIntent(target: Intent, name: String?, value: String): Intent {
        error("not implemented")
    }

    override fun serialize(value: String): String {
        return value
    }

    override fun deserialize(serializedValue: String): String {
        return serializedValue
    }

    override fun createStorageDataCache(provider: StorageProvider): StorageDataCache {
        return DemoStorageDataCache(provider)
    }
}
