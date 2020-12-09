package at.searles.sourceeditor.storage

import android.content.Context
import android.content.Intent
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider
import at.searles.sourceeditor.SourceEditorActivity

class SourceEditorStorageEditor(private val context: Context, provider: StorageProvider, callback: StorageEditorCallback<String>): StorageEditor<String>(provider, callback, SourceEditorStorageManageActivity::class.java) {
    override fun createReturnIntent(target: Intent, name: String?, value: String): Intent {
        return target.apply {
            putExtra(SourceEditorActivity.sourceKey, value)
        }
    }

    override fun createStorageDataCache(provider: StorageProvider): StorageDataCache {
        return SourceEditorStorageDataCache(context, provider)
    }

    override fun deserialize(serializedValue: String): String {
        return serializedValue
    }

    override fun serialize(value: String): String {
        return value
    }
}