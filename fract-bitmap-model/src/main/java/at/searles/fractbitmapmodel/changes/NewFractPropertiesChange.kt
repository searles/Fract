package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties

/**
 * Use this one to load favorites
 */
class NewFractPropertiesChange(private val newProperties: FractProperties, override val addToHistory: Boolean = true): CalcPropertiesChange {
    override fun accept(properties: FractProperties): FractProperties {
        return newProperties
    }
}