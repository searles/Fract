package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractBitmapModel

/**
 * Changes of the bitmap model, eg the whole bitmap's size was modified. These changes
 * are applied when the calculation is stopped.
 */
interface BitmapModelChange {
    fun accept(model: FractBitmapModel)
}