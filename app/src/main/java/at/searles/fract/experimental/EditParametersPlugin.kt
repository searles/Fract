package at.searles.fract.experimental

import android.content.Context
import android.graphics.Canvas
import android.view.MotionEvent
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.ScalableImageView

// TODO it is much easier if they are stateful
// TODO: This requires a savings mechanism though.
// TODO: Which means that there should be a plugin service.
// TODO: On the other hand, I want to show the color of parameters on screen
class EditParametersPlugin(private val context: Context, private val bitmapModel: FractBitmapModel): Plugin {

    override val isEnabled = true

    override fun onDraw(source: ScalableImageView, canvas: Canvas) {
        TODO("Not yet implemented")
    }

    override fun onTouchEvent(source: ScalableImageView, event: MotionEvent): Boolean {
        TODO("Not yet implemented")
    }
}

/*
Design considerations:

There are multiple bitmapModels.
There is a list of ids of shared parameters.

Where is that list? It must be accessible from parameterAdapter.
parameterAdapter is created in FractMainActivity. Whenever parameters change,
the update-function in parameterAdapter is invoked.

Better design:

ParameterFragment:
    provides adapter
    maintains sharedList
    links to all bitmapModels.



 */