package at.searles.sourceeditor

import android.os.Handler
import android.os.SystemClock
import android.util.Log

/**
 * Calls the update task 'delay' ms after the latest tick. This class
 * uses Handler, thus all tick-calls must come from the same thread.
 */
class DelayedUpdater(private val updateTask: Runnable, private val delay: Long) {

    private var updateTaskScheduled: Boolean = false
    private var nextUpdateTimeStamp: Long = -1

    private var updateHandler: Handler? = null

    private fun runUpdate() {
        check(updateTaskScheduled)

        val currentTimeStamp = SystemClock.uptimeMillis()

        if(nextUpdateTimeStamp > currentTimeStamp) {
            // there was a more recent tick.
            updateHandler!!.postDelayed({ runUpdate() }, nextUpdateTimeStamp - currentTimeStamp)
            return
        }

        updateTaskScheduled = false
        updateTask.run()
    }

    fun isScheduled(): Boolean = updateTaskScheduled

    fun tick() {
        if(updateHandler == null) {
            // create on first run.
            updateHandler = Handler()
        }

        nextUpdateTimeStamp = SystemClock.uptimeMillis() + delay

        if(!updateTaskScheduled) {
            updateHandler!!.postDelayed({ runUpdate() }, delay)
            updateTaskScheduled = true
        }
    }

    fun cancel() {
        Log.d("DelayedUpdater", "cancel")
        updateHandler?.removeCallbacksAndMessages(null)
        updateTaskScheduled = false
    }
}
