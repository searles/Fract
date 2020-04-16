package at.searles.fract

import android.content.Context
import at.searles.fract.demos.AssetsUtils
import at.searles.fractbitmapmodel.FractProperties

object FactorySettings {
    const val factoryIsTouchEnabled = true
    const val factoryIsConfirmZoom = false
    const val factoryIsRotationLock = false

    const val factoryIsGridEnabled = false
    const val factoryIsEditLightsOnScreenEnabled = false

    const val factoryWidth = 1600
    const val factoryHeight = 900

    // use default source
    fun getStartupFractal(context: Context): FractProperties {
        val sourceCode = AssetsUtils.readAssetSource(context, "mandelbrot")
        return FractProperties.create(sourceCode, emptyMap(), null, null, emptyList())
    }
}