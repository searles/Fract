package at.searles.fract

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.renderscript.RenderScript
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import at.searles.commons.math.Scale
import at.searles.fract.demos.DemosFoldersHolder
import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.CalculationTaskBitmapModel
import at.searles.fractbitmapmodel.CalculationTaskFactory
import at.searles.fractbitmapmodel.Shader3DProperties
import at.searles.fractbitmapmodel.tasks.BitmapModelParameters
import at.searles.fractimageview.ScalableImageView
import at.searles.fractlang.CompilerInstance
import at.searles.itemselector.ItemSelectorActivity
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

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

    private lateinit var calculationTaskFactory: CalculationTaskFactory
    private lateinit var bitmapModel: CalculationTaskBitmapModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuNavigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.demoAction -> { openDemoActivity() }
                R.id.openFavoritesAction -> {}
                R.id.shareAction -> {}
                R.id.addToFavoritesAction -> {}
                R.id.openSettingsAction -> {}
            }

            drawerLayout.closeDrawers()
            true
        }


        initBitmapModel()

        mainImageView.scalableBitmapModel = bitmapModel
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode != Activity.RESULT_OK) {
            return
        }

        when(requestCode) {
            demoSelectorRequestCode -> {
                require(data != null)
                val sourceId = data.getStringExtra(ItemSelectorActivity.folderNameKey)!!
                val parameterId = data.getStringExtra(ItemSelectorActivity.itemNameKey)!!

                openDemo(sourceId, parameterId)
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openDemo(sourceId: String, parameterId: String) {
        // TODO

    }

    private fun openDemoActivity() {
        Intent(this, ItemSelectorActivity::class.java).also {
            it.putExtra(ItemSelectorActivity.initializerClassNameKey, DemosFoldersHolder::class.java.canonicalName)
            startActivityForResult(it, demoSelectorRequestCode)
        }
    }

    private fun initBitmapModel() {
        val palettes = listOf(
            Palette(5, 2, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(1, Rgb(0f, 0f, 0f).toLab())
                        row.put(2, Rgb(1f, 0f, 0f).toLab())
                        row.put(3, Rgb(1f, 1f, 0f).toLab())
                        row.put(4, Rgb(1f, 1f, 1f).toLab())
                        row.put(0, Rgb(0f, 0f, 1f).toLab())
                    })
                    table.put(1, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(1f, 1f, 1f).toLab())
                        row.put(1, Rgb(0f, 0.5f, 0f).toLab())
                        row.put(2, Rgb(0f, 0.25f, 1f).toLab())
                        row.put(3, Rgb(0.5f, 0.12f, 0.05f).toLab())
                        row.put(4, Rgb(0f, 0f, 0f).toLab())
                    })
                }),
            Palette(1, 1, 0f, 0f,
                SparseArray<SparseArray<Lab>>().also { table ->
                    table.put(0, SparseArray<Lab>().also { row ->
                        row.put(0, Rgb(0f, 0f, 0f).toLab())
                    })
                })
        )

        val shaderProperties = Shader3DProperties()

        val compilerInstance = CompilerInstance(
            "setResult(0, sin rad point, sin rad point);",
            emptyMap()
        ).apply {
            compile()
            Log.i("COMPILED", this.vmCode.toString())
        }

        val fractal = BitmapModelParameters(Scale(2.0, 0.0, 0.0, 2.0, 0.0, 0.0),
            palettes,
            shaderProperties,
            compilerInstance
        )

        val rs = RenderScript.create(this)

        val bitmapAllocation = BitmapAllocation(rs, 1000,600)

        calculationTaskFactory = CalculationTaskFactory(rs, fractal, bitmapAllocation)

        bitmapModel = CalculationTaskBitmapModel(calculationTaskFactory).apply {
            listener = object: CalculationTaskBitmapModel.Listener {
                var timerStart: Long = 0

                override fun started() {
                    mainImageView.invalidate()
                    timerStart = System.currentTimeMillis()
                }

                override fun progress(progress: Float) {
                }

                override fun bitmapUpdated() {
                    mainImageView.invalidate()
                }

                override fun finished() {
                    mainImageView.invalidate()
                    Log.d("TIMER", "duration: ${System.currentTimeMillis() - timerStart}")
                }
            }
        }
    }

    companion object {
        val demoSelectorRequestCode = 152
    }
}