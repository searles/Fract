package at.searles.itemselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.searles.itemselector.model.Folder
import at.searles.itemselector.model.FolderModel
import at.searles.itemselector.model.FoldersHolder
import at.searles.itemselector.model.Item

/**
 * Similar to Storage, but only to select items from a given folder structure.
 * No selection needed.
 */
class ItemSelectorActivity : AppCompatActivity(), FolderAdapter.Listener {

    private val recyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.contentRecyclerView).apply {
            setHasFixedSize(true)
        }
    }

    private val toolbar: Toolbar by lazy {
        findViewById<Toolbar>(R.id.toolbar)
    }

    private lateinit var adapter: FolderAdapter
    private lateinit var model: FolderModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_selector_activity_main)
        setSupportActionBar(toolbar)

        // set up data
        createModel()

        // set up data structures for viewing items
        adapter = FolderAdapter(this, model).apply {
            listener = this@ItemSelectorActivity
        }

        // and now for the recycler view
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ItemSelectorActivity.adapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun createModel() {
        val clazzName = intent.getStringExtra(initializerClassNameKey)!!

        @Suppress("UNCHECKED_CAST")
        val clazz = Class.forName(clazzName) as Class<FoldersHolder>
        val ctor = clazz.getConstructor(Context::class.java)

        val initializer = ctor.newInstance(this)

        model = FolderModel(initializer.folders)
    }

    override fun itemClicked(folder: Folder, item: Item) {
        val intent = Intent().apply {
            putExtra(folderKey, folder.key)
            putExtra(itemKey, item.key)
            putExtra(specialKey, false)
        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun itemLongClicked(folder: Folder, item: Item) {
        val intent = Intent().apply {
            putExtra(folderKey, folder.key)
            putExtra(itemKey, item.key)
            putExtra(specialKey, true)
        }

        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    companion object {
        const val initializerClassNameKey = "initializerClassName"
        const val folderKey = "folder"
        const val itemKey = "item"
        const val specialKey = "special"
    }

}
