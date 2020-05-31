package at.searles.fract

import android.content.Context
import at.searles.fract.demos.AssetsUtils
import at.searles.fractbitmapmodel.FractProperties

object FactorySettings {
    val mode = FractSettings.Mode.Scale

    const val factoryIsConfirmZoom = false
    const val factoryIsRotationLock = false
    const val factoryIsCenterLock: Boolean = false

    const val factoryIsGridEnabled = false

    // default is 720p
    const val factoryWidth = 1280
    const val factoryHeight = 720

    // use default source
    fun getStartupFractal(context: Context): FractProperties {
        val sourceCode = AssetsUtils.readAssetSource(context, "mandelbrot")
        return FractProperties.create(sourceCode, emptyMap(), null, null, emptyMap())
    }
}