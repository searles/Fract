package at.searles.fractimageview

import android.graphics.Canvas
import android.view.MotionEvent

interface Plugin {
    val isEnabled: Boolean
    fun onDraw(source: PluginScalableImageView, canvas: Canvas)
    fun onTouchEvent(source: PluginScalableImageView, event: MotionEvent): Boolean
    fun onLayoutChanged(source: PluginScalableImageView) {}
}