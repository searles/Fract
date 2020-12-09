package at.searles.android.storage

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import at.searles.android.storage.data.StorageDataCache
import at.searles.android.storage.data.StorageProvider
import at.searles.android.storage.dialog.FinishWithoutSavingDialogFragment
import at.searles.android.storage.dialog.ForceOpenDialogFragment
import at.searles.android.storage.dialog.SaveStrategyQuestionDialog
import at.searles.android.storage.dialog.SaveAsDialogFragment

/**
 * This class encapsulates all necessary methods to maintain a
 * Open/Save/Save As-structure in an activity. Such activities must
 * implement the StorageEditorCallback-interface.
 *
 * XXX if ever needed that an activity must provide two storage editors,
 * then there should be anonther interface with 'getStorageEditor(id)'.
 */
abstract class StorageEditor<A>(
    private val provider: StorageProvider,
    private val callback: StorageEditorCallback<A>,
    private val storageManagerActivityClass: Class<out StorageManagerActivity>) {

    private val activity = callback as AppCompatActivity

    private var name: String? = null
    private var isModified = false

    abstract fun createStorageDataCache(provider: StorageProvider): StorageDataCache

    fun getReturnIntent(target: Intent): Intent {
        return createReturnIntent(target, name, callback.value)
    }

    protected abstract fun createReturnIntent(target: Intent, name: String?, value: A): Intent

    val storageDataCache by lazy { createStorageDataCache(provider) }

    fun onSave() {
        if(name == null) {
            Log.i(javaClass.simpleName, "Save was clicked although 'name' is null")
            Toast.makeText(activity, activity.getString(R.string.cannotSaveBecauseNoName), Toast.LENGTH_LONG).show()
            return
        }

        if(!isModified) {
            Log.i(javaClass.simpleName, "Save was clicked although it is not modified")
        }

        try {
            provider.save(name!!, serialize(callback.value))
            storageDataCache.invalidate()
        } catch (th: Throwable) {
            Toast.makeText(activity, activity.resources.getString(R.string.errorWithMsg, th.localizedMessage), Toast.LENGTH_LONG).show()
            return
        }

        isModified = false
        callback.onStorageItemChanged(name, isModified)
    }

    abstract fun serialize(value: A): String
    
    abstract fun deserialize(serializedValue: String): A

    fun onOpen(requestCode: Int) {
        val intent = Intent(activity, storageManagerActivityClass)

        activity.startActivityForResult(intent, requestCode)
    }

    fun open(newName: String) {
        if (!isModified) {
            forceOpen(newName)
        } else {
            ForceOpenDialogFragment.newInstance(newName).show(activity.supportFragmentManager, "dialog")
        }
    }

    fun forceOpen(newName: String) {
        // this is also called if nothing has to be discarded.
        try {
            val value = deserialize(provider.load(newName))
            callback.value = value

            isModified = false
            this.name = newName

            callback.onStorageItemChanged(newName, isModified)
        } catch(th: Throwable) {
            Toast.makeText(activity, activity.resources.getString(R.string.errorWithMsg, th.localizedMessage), Toast.LENGTH_LONG).show()
            return
        }
    }

    fun onSaveAs() {
        SaveAsDialogFragment.newInstance(name ?: "").show(activity.supportFragmentManager, "dialog")
    }

    fun saveAs(name: String) {
        if(provider.exists(name)) {
            SaveStrategyQuestionDialog.newInstance(name).show(activity.supportFragmentManager, "dialog")
            return
        }

        forceSaveAs(name)
    }

    fun forceSaveAs(newName: String) {
        try {
            provider.save(newName, serialize(callback.value))
            storageDataCache.invalidate()

            isModified = false
            this.name = newName

            callback.onStorageItemChanged(newName, isModified)
        } catch(th: Throwable) {
            Toast.makeText(activity, activity.resources.getString(R.string.errorWithMsg, th.localizedMessage), Toast.LENGTH_LONG).show()
            return
        }
    }

    /**
     * Useful right after creation to update the UI
     */
    fun fireStorageItemStatus() {
        callback.onStorageItemChanged(name, isModified)
    }

    fun notifyValueModified() {
        isModified = true
        fireStorageItemStatus()
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putString(nameKey, name)
        outState.putBoolean(isModifiedKey, isModified)
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        if(savedInstanceState != null) {
            name = savedInstanceState.getString(nameKey)
            isModified = savedInstanceState.getBoolean(isModifiedKey)

            // Caller must ensure that fireStatus is called.
        }
    }

    /**
     * Returns true if activity can be closed, otherwise false. In the latter case,
     * a dialog is displayed that will take care of closing the activity.
     */
    fun onFinish(isActivityCanceled: Boolean): Boolean {
        if(name != null && isModified) {
            FinishWithoutSavingDialogFragment.newInstance(isActivityCanceled).show(activity.supportFragmentManager, "dialog")
            return false
        }

        (activity as StorageEditorCallback<*>).storageEditor.finishWithoutSaving(isActivityCanceled)
        return true
    }

    internal fun finishWithoutSaving(isActivityCanceled: Boolean) {
        if(isActivityCanceled) {
            activity.setResult(Activity.RESULT_CANCELED, activity.intent)
        } else {
            activity.setResult(Activity.RESULT_OK, createReturnIntent(activity.intent, name, callback.value))
        }
        activity.finish()
    }

    fun invalidate() {
        storageDataCache.invalidate()
    }

    companion object {
        private const val nameKey = "name"
        private const val isModifiedKey = "isModified"
    }
}

