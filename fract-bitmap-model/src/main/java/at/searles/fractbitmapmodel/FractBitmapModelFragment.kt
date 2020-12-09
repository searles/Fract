package at.searles.fractbitmapmodel

import android.os.Bundle
import android.renderscript.RenderScript
import android.widget.Toast
import androidx.fragment.app.Fragment
import at.searles.fractbitmapmodel.changes.BitmapAllocationChange

class FractBitmapModelFragment : Fragment() {

    private lateinit var rs: RenderScript

    lateinit var bitmapModel: FractBitmapModel
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        initialize(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(propertiesKey, FractPropertiesAdapter.toBundle(bitmapModel.properties))
        outState.putInt(widthKey, bitmapModel.width)
        outState.putInt(heightKey, bitmapModel.height)
    }

    private fun initialize(savedInstanceState: Bundle?) {
        val dataBundle = savedInstanceState ?: arguments!!

        val propertiesBundle = dataBundle.getBundle(propertiesKey)!!
        val width = dataBundle.getInt(widthKey)
        val height = dataBundle.getInt(heightKey)

        val properties = FractPropertiesAdapter.fromBundle(propertiesBundle)

        rs = RenderScript.create(context)

        val bitmapAllocation = BitmapAllocation(rs, width, height)

        bitmapModel = FractBitmapModel(rs, bitmapAllocation, properties)
        bitmapModel.startTask()
    }

    fun addImageSizeChange(width: Int, height: Int): Boolean {
        return try {
            val newBitmapAllocation = BitmapAllocation(rs, width, height)
            bitmapModel.scheduleBitmapModelChange(BitmapAllocationChange(newBitmapAllocation))
            true
        } catch(th: Throwable) {
            Toast.makeText(context, th.localizedMessage, Toast.LENGTH_LONG).show()
            false
        }
    }


    companion object {
        const val propertiesKey = "properties"
        const val widthKey = "width"
        const val heightKey ="height"

        fun createInstance(properties: FractProperties, width: Int, height: Int): FractBitmapModelFragment {
            val propertiesBundle = FractPropertiesAdapter.toBundle(properties)

            val bundle = Bundle().apply {
                putBundle(propertiesKey, propertiesBundle)
                putInt(widthKey, width)
                putInt(heightKey, height)
            }


            return FractBitmapModelFragment().apply {
                arguments = bundle
            }
        }
    }
}