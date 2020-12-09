package at.searles.paletteeditor

import android.content.Context
import android.util.TypedValue


object ThemeUtils {
    fun getThemeAccentColor(context: Context): Int {
        // thanks to https://stackoverflow.com/questions/27611173/how-to-get-accent-color-programmatically
        val outValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorAccent, outValue, true)
        return outValue.data
    }
}