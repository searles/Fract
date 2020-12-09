package at.searles.fractimageview.plugins

import android.graphics.Canvas
import android.view.MotionEvent
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.PluginScalableImageView

class GestureBlockPlugin: Plugin {

    override var isEnabled: Boolean = true

    override fun onDraw(source: PluginScalableImageView, canvas: Canvas) {
        // XXX maybe some icon?
    }

    override fun onTouchEvent(source: PluginScalableImageView, event: MotionEvent): Boolean {
        return isEnabled
    }
}