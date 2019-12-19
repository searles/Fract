package at.searles.fract

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import at.searles.fract.demos.AssetsUtils
import at.searles.fract.demos.DemosFolderHolder
import at.searles.fractbitmapmodel.BitmapSync
import at.searles.fractbitmapmodel.CalcBitmapModel
import at.searles.fractbitmapmodel.tasks.SourceCodeChange
import at.searles.fractimageview.ScalableImageView
import at.searles.itemselector.ItemSelectorActivity
import com.google.android.material.navigation.NavigationView

class FractMainActivity : AppCompatActivity(), BitmapSync.Listener, CalcBitmapModel.Listener, TaskBitmapModelFragment.Listener {

    private lateinit var bitmapModelFragment: TaskBitmapModelFragment

    private val menuNavigationView: NavigationView by lazy {
        findViewById<NavigationView>(R.id.menuNavigationView)
    }

    private val drawerLayout: DrawerLayout by lazy {
        findViewById<DrawerLayout>(R.id.drawerLayout)
    }

    private val parameterRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.parameterRecyclerView)
    }

    private val mainImageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.mainImageView)
    }

    private val taskProgressBar: ProgressBar by lazy {
        findViewById<ProgressBar>(R.id.taskProgressBar).apply {
            isIndeterminate = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuNavigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.demoAction -> {
                    openDemoActivity()
                }
                R.id.openFavoritesAction -> {
                }
                R.id.shareAction -> {
                }
                R.id.addToFavoritesAction -> {
                }
                R.id.openSettingsAction -> {
                }
            }

            drawerLayout.closeDrawers()
            true
        }

        initBitmapModelFragment()

        mainImageView.visibility = View.INVISIBLE
        taskProgressBar.visibility = View.VISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            demoSelectorRequestCode -> {
                require(data != null)
                val sourceId = data.getStringExtra(ItemSelectorActivity.folderKey)!!
                val parameterId = data.getStringExtra(ItemSelectorActivity.itemKey)!!

                openDemo(sourceId, parameterId)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openDemo(sourceKey: String, parametersKey: String) {
        val sourceCode = AssetsUtils.readAssetSource(this, sourceKey)
        val parameters = AssetsUtils.readAssetParameters(this, parametersKey)

        val change = SourceCodeChange(sourceCode, parameters)

        bitmapModelFragment.bitmapModel.addCalcChange(change)
        bitmapModelFragment.bitmapModel.addPostCalcChange(change)
    }

    private fun openDemoActivity() {
        Intent(this, ItemSelectorActivity::class.java).also {
            it.putExtra(ItemSelectorActivity.initializerClassNameKey, DemosFolderHolder::class.java.canonicalName)
            startActivityForResult(it, demoSelectorRequestCode)
        }
    }

    override fun onResume() {
        super.onResume()

        if(!bitmapModelFragment.isInitializing) {
            connectBitmapModelFragment()
            taskProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        if(!bitmapModelFragment.isInitializing) {
            bitmapModelFragment.listener = null
        }

        super.onDestroy()
    }

    private fun initBitmapModelFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(taskBitmapModelFragmentTag)

        bitmapModelFragment = fragment as TaskBitmapModelFragment? ?: with(TaskBitmapModelFragment.createInstance()) {
            supportFragmentManager.beginTransaction().add(this, taskBitmapModelFragmentTag).commit()
            this
        }

        bitmapModelFragment.initListener = this
    }

    override fun initializationFinished() {
        connectBitmapModelFragment()
    }

    private fun connectBitmapModelFragment() {
        mainImageView.scalableBitmapModel = bitmapModelFragment.bitmapModel
        mainImageView.visibility = View.VISIBLE
        mainImageView.invalidate()

        bitmapModelFragment.listener = this

        taskProgressBar.apply {
            min = -progressBarZero
            max = progessBarFactor
            isIndeterminate = false
        }
    }

    override fun started() {
        taskProgressBar.apply {
            visibility = View.VISIBLE
            progress = 0
        }
    }

    override fun bitmapUpdated() {
        mainImageView.invalidate()
    }

    override fun finished() {
        taskProgressBar.visibility = View.INVISIBLE
    }

    override fun progress(progress: Float) {
        taskProgressBar.visibility = View.VISIBLE
        taskProgressBar.progress = (progessBarFactor * progress).toInt()
    }

    companion object {
        private const val progessBarFactor = 900
        private const val progressBarZero = 100
        private const val demoSelectorRequestCode = 152
        private const val taskBitmapModelFragmentTag = "bitmapModel"
    }

}