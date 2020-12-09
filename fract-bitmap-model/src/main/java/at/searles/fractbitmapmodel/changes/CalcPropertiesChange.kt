package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties

/**
 * Modifications of the calculation, ie, the calculation must be restarted.
 * These changes create a new instance of FractProperties based on
 * the previous one and schedule it for future modifications.
 */
interface CalcPropertiesChange {
    val addToHistory: Boolean
        get() = true

    fun accept(properties: FractProperties): FractProperties
}