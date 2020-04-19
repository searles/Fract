package at.searles.fract

import android.content.Context
import at.searles.fract.demos.AssetsUtils
import at.searles.fractbitmapmodel.FractProperties

object FactorySettings {
    val mode = FractSettings.Mode.Scale

    const val factoryIsConfirmZoom = false
    const val factoryIsRotationLock = false

    const val factoryIsGridEnabled = false

    const val factoryWidth = 1600
    const val factoryHeight = 900

    // use default source
    fun getStartupFractal(context: Context): FractProperties {
        val sourceCode = AssetsUtils.readAssetSource(context, "mandelbrot")
        return FractProperties.create(sourceCode, emptyMap(), null, null, emptyMap())
    }
}