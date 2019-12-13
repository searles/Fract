package at.searles.fract

import android.os.Bundle
import android.os.Handler
import android.renderscript.RenderScript
import android.util.Log
import android.util.SparseArray
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.CalculationTaskBitmapModel
import at.searles.fractbitmapmodel.CalculationTaskFactory
import at.searles.fractbitmapmodel.tasks.BitmapModelParameters
import at.searles.fractimageview.ScalableImageView
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb
import com.google.android.material.navigation.NavigationView
import kotlin.math.sin

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
            // close drawer when item is tapped
            drawerLayout.closeDrawers()
            true
        }


        initBitmapModel()

        mainImageView.scalableBitmapModel = bitmapModel

        startRotation()
    }

    private fun initBitmapModel() {
        val fractal = BitmapModelParameters(
            Scale(2.0, 0.0, 0.0, 2.0, 0.0, 0.0),
            palettes = listOf(
                Palette(5, 2, 0f, 0f,
                    SparseArray<SparseArray<Lab>>().also { table ->
                        table.put(0, SparseArray<Lab>().also { row ->
                            row.put(0, Rgb(0f, 0f, 0f).toLab())
                            row.put(1, Rgb(1f, 0f, 0f).toLab())
                            row.put(2, Rgb(1f, 1f, 0f).toLab())
                            row.put(3, Rgb(1f, 1f, 1f).toLab())
                            row.put(4, Rgb(0f, 0f, 1f).toLab())
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
        )

        val rs = RenderScript.create(this)

        val bitmapAllocation = BitmapAllocation(rs, 2000, 1200)

        calculationTaskFactory = CalculationTaskFactory(rs, fractal, bitmapAllocation)

        bitmapModel = CalculationTaskBitmapModel(calculationTaskFactory).apply {
            listener = object : CalculationTaskBitmapModel.Listener {
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

    var alpha = 0.0f

    val handler: Handler by lazy { Handler() }
    
    fun startRotation() {
        calculationTaskFactory.minPixelGap = animationPixelGap

        val task = object : Runnable {
            override fun run() {
                val shader = calculationTaskFactory.shader3DProperties
                shader.setLightVector(sin(0.782f * alpha), alpha)

                calculationTaskFactory.shader3DProperties = shader
                calculationTaskFactory.setPaletteOffset(0, alpha * 0.17f, alpha * 0.03f)

                alpha += 0.05f
                calculationTaskFactory.syncBitmap()
                mainImageView.invalidate()
                handler.postDelayed(this, 40)
            }
        }

        handler.postDelayed(task, 40)
    }

    companion object {
        val animationPixelGap = 4
    }
}