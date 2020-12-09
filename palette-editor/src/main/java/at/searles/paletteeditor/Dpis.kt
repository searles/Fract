package at.searles.paletteeditor

import android.content.res.Resources
import android.util.TypedValue




object Dpis {
    /**
     * @thanks https://stackoverflow.com/questions/4605527/converting-pixels-to-dp
     */
    fun dpiToPx(r: Resources, dip: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dip,
            r.displayMetrics
        )
    }
}