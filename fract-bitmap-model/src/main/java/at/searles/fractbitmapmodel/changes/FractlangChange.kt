package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractlang.FractlangProgram

/**
 * Use this one if the source code and/or parameters change
 * but everything else remains the same. Parameters
 * are either the existing ones or empty ones.
 * All other parameters, eg palette or scale, are kept.
 */
class FractlangChange(private val fractlangProgram: FractlangProgram): CalcPropertiesChange {
    override fun accept(properties: FractProperties): FractProperties {
        return properties.createWithNewProperties(fractlangProgram)
    }
}