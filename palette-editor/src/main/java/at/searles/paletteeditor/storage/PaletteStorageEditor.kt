package at.searles.paletteeditor.storage

import android.content.Context
import android.content.Intent
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider
import at.searles.commons.color.Palette
import at.searles.paletteeditor.PaletteAdapter
import at.searles.paletteeditor.PaletteEditorActivity
import org.json.JSONObject

class PaletteStorageEditor(private val context: Context, provider: StorageProvider, callback: StorageEditorCallback<Palette>): StorageEditor<Palette>(provider, callback, PaletteStorageManageActivity::class.java) {
    override fun createReturnIntent(target: Intent, name: String?, value: Palette): Intent {
        target.putExtra(PaletteEditorActivity.paletteKey, PaletteAdapter.toBundle(value))
        return target
    }

    override fun createStorageDataCache(provider: StorageProvider): StorageDataCache {
        return PaletteStorageDataCache(provider, context)
    }

    override fun deserialize(serializedValue: String): Palette {
        return PaletteAdapter.toPalette(JSONObject(serializedValue))
    }

    override fun serialize(value: Palette): String {
        return PaletteAdapter.toJson(value).toString(4)
    }
}