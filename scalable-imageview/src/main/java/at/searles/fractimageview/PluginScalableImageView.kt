package at.searles.fractimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent

class PluginScalableImageView(context: Context, attrs: AttributeSet) : ScalableImageView(context, attrs) {
    private val plugins = ArrayList<Plugin>()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if(!changed) {
            return
        }

        super.onLayout(changed, left, top, right, bottom)
        for (plugin in plugins) {
            plugin.onLayoutChanged(this)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        plugins.forEach {
            if(it.isEnabled) {
                it.onDraw(this, canvas)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        plugins.forEach {
            if (it.isEnabled && it.onTouchEvent(this, event)) return true
        }

        return super.onTouchEvent(event)
    }

    fun addPlugin(plugin: Plugin) {
        this.plugins.add(0, plugin)
    }
}