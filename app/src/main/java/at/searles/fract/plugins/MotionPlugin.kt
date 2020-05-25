package at.searles.fract.plugins

import android.view.MotionEvent
import android.view.MotionEvent.INVALID_POINTER_ID
import at.searles.fractimageview.Plugin
import at.searles.fractimageview.PluginScalableImageView

/**
 * Check https://developer.android.com/training/gestures/scale
 */
abstract class MotionPlugin: Plugin {
    // The ‘active pointer’ is the one currently moving our object.
    protected var lastTouchX: Float = -1f
    protected var lastTouchY: Float = -1f

    protected var currentTouchX: Float = -1f
    protected var currentTouchY: Float = -1f

    protected val deltaX: Float
        get() = currentTouchX - lastTouchX
    protected val deltaY: Float
        get() = currentTouchY - lastTouchY

    private var activePointerId: Int = INVALID_POINTER_ID

    val isActive: Boolean
        get() = activePointerId != INVALID_POINTER_ID

    override fun onTouchEvent(source: PluginScalableImageView, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                event.actionIndex.also { pointerIndex ->
                    // Remember where we started (for dragging)
                    lastTouchX = event.getX(pointerIndex)
                    lastTouchY = event.getY(pointerIndex)

                    currentTouchX = lastTouchX
                    currentTouchY = lastTouchY
                }

                // Save the ID of this pointer (for dragging)
                activePointerId = event.getPointerId(0)

                activatePlugin(source)
            }

            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = event.findPointerIndex(activePointerId)

                if(pointerIndex == -1) {
                    return false
                }

                lastTouchX  = currentTouchX
                lastTouchY = currentTouchY

                currentTouchX = event.getX(pointerIndex)
                currentTouchY = event.getY(pointerIndex)

                movePointer(source)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                deactivatePlugin()
            }

            MotionEvent.ACTION_POINTER_UP -> {
                event.actionIndex.also { pointerIndex ->
                    event.getPointerId(pointerIndex)
                        .takeIf { it == activePointerId }
                        ?.run {
                            // This was our active pointer going up. Choose a new
                            // active pointer and adjust accordingly.
                            val newPointerIndex = if (pointerIndex == 0) 1 else 0

                            lastTouchX = event.getX(newPointerIndex)
                            lastTouchY = event.getY(newPointerIndex)

                            currentTouchX = lastTouchX
                            currentTouchY = lastTouchY

                            activePointerId = event.getPointerId(newPointerIndex)
                        }
                }
            }
        }

        return true
    }

    abstract fun movePointer(source: PluginScalableImageView)

    abstract fun deactivatePlugin()

    abstract fun activatePlugin(source: PluginScalableImageView)
}