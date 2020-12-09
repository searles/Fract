package at.searles.fractbitmapmodel.changes

import android.graphics.Matrix
import at.searles.fractbitmapmodel.FractProperties

class RelativeScaleChange(private val relativeMatrix: Matrix): CalcPropertiesChange {
    override fun accept(properties: FractProperties): FractProperties {
        return properties.createWithRelativeScale(relativeMatrix)
    }
}