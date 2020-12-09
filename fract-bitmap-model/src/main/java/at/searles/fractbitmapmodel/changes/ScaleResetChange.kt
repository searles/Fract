package at.searles.fractbitmapmodel.changes

import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.FractProperties

class ScaleResetChange(private val scale: Scale): CalcPropertiesChange {
    override fun accept(properties: FractProperties): FractProperties {
        return FractProperties(properties.program, null, properties.customShaderProperties, properties.customPalettes)
    }
}