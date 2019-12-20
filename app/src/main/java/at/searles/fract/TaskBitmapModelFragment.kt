package at.searles.fract

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.renderscript.RenderScript
import android.util.SparseArray
import android.widget.Toast
import androidx.fragment.app.Fragment
import at.searles.commons.math.Scale
import at.searles.fract.demos.AssetsUtils
import at.searles.fractbitmapmodel.*
import at.searles.fractbitmapmodel.tasks.CalcChange
import at.searles.fractbitmapmodel.tasks.ControllerChange
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.PaletteData
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.colors.Lab
import at.searles.paletteeditor.colors.Rgb

class TaskBitmapModelFragment : Fragment() {

    private lateinit var rs: RenderScript

    lateinit var bitmapModel: CalcBitmapModel
        private set

    val bitmap: Bitmap
        get() = bitmapModel.bitmap

    lateinit var calcProperties: CalcProperties
    lateinit var bitmapProperties: BitmapProperties
    private lateinit var controller: CalcController

    var isInitializing: Boolean = true
        private set

    var initListener: Listener? = null

    var listener: CalcBitmapModel.Listener?
        get() {
            return bitmapModel.listener
        }

        set(value) {
            bitmapModel.listener = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        asyncInitialize()
    }

    private fun asyncInitialize() {
        // This should be fast enough to not raise problems wrt leaks.
        @SuppressLint("StaticFieldLeak")
        val task = object: AsyncTask<Unit, Unit, Unit>() {
            override fun doInBackground(vararg params: Unit?) {
                initBitmapModel()
            }

            override fun onPostExecute(result: Unit?) {
                isInitializing = false
                initListener?.initializationFinished()
                bitmapModel.startTask()
            }
        }

        task.execute()
    }

    private fun initBitmapModel() {
        rs = RenderScript.create(context)
        calcProperties = CalcProperties(initialScale, FractlangProgram(initialSourceCode, emptyMap()))
        bitmapProperties = BitmapProperties(initialPalettes, initialShader)

        val bitmapAllocation = BitmapAllocation(rs, 1000,600)

        controller = CalcController(rs, calcProperties, bitmapProperties, bitmapAllocation)
        bitmapModel = CalcBitmapModel(controller)
    }

    fun addSetImageSizeChange(width: Int, height: Int) {
        try {
            val newBitmapAllocation = BitmapAllocation(rs, width, height)

            bitmapModel.addPostCalcChange(object: ControllerChange {
                override fun accept(controller: CalcController) {
                    controller.bitmapAllocation = newBitmapAllocation
                }
            })
        } catch(th: Throwable) {
            Toast.makeText(context, getString(R.string.exceptionMessage, th.localizedMessage), Toast.LENGTH_LONG).show()
        }
    }

    fun addScaleChange(scale: Scale) {
        bitmapModel.addCalcChange(object: CalcChange {
            override fun accept(calcProperties: CalcProperties): CalcProperties {
                return calcProperties.createWithNewScale(scale)
            }
        })
    }

    /**
     * This one is called from favorites.
     */
    fun addSetPropertiesChange(calcProperties: CalcProperties, bitmapProperties: BitmapProperties) {
        val change = object: CalcChange, ControllerChange {
            override fun accept(calcProperties: CalcProperties): CalcProperties {
                return calcProperties
            }

            override fun accept(controller: CalcController) {
                controller.bitmapProperties = bitmapProperties
            }
        }

        bitmapModel.addCalcChange(change)
        bitmapModel.addPostCalcChange(change)
    }

    fun setPalette(index: Int, palette: Palette) {
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val initialSourceCode get() = AssetsUtils.readAssetSource(context!!, "mandelbrot")
    private val initialScale = Scale(2.0, 0.0, 0.0, 2.0, 0.0, 0.0)
    private val initialShader = ShaderProperties()
    private val initialPalettes = listOf(
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

    interface Listener {
        fun initializationFinished()
    }

    companion object {
        fun createInstance(): TaskBitmapModelFragment {
            return TaskBitmapModelFragment()
        }
    }
}