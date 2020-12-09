package at.searles.itemselector.demo

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import at.searles.itemselector.ItemSelectorActivity

class MainActivity : AppCompatActivity() {

    private val button: Button by lazy {
        findViewById<Button>(R.id.button)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            Intent(this, ItemSelectorActivity::class.java).also {
                it.putExtra(ItemSelectorActivity.initializerClassNameKey, DemoInitializer::class.java.canonicalName)
                startActivityForResult(it, demoSelectorRequestCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == demoSelectorRequestCode) {
            if(resultCode == Activity.RESULT_OK) {
                require(data != null)

                val folderKey = data.getStringExtra(ItemSelectorActivity.folderKey)!!
                val itemKey = data.getStringExtra(ItemSelectorActivity.itemKey)!!

                Log.i("DemoSelector", "folder: $folderKey, item: $itemKey")

                return
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val demoSelectorRequestCode = 111
    }
}
