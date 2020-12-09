package at.searles.fractbitmapmodel

import android.os.Handler
import android.renderscript.RenderScript
import android.util.Log
import kotlin.math.max

class BitmapController(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation
) {
    private var bitmapScript: ScriptC_bitmap? = null
    private var interpolateGapsScript: ScriptC_interpolate_gaps? = null
    private var paletteUpdater: PaletteToScriptUpdater? = null
    private val bitmapUpdater = BitmapUpdater()

    private var isInitialized = false

    var listener: Listener? = null

    var bitmapAllocation: BitmapAllocation = initialBitmapAllocation
        set(value) {
            field.rsBitmap.destroy()
            field = value
            bindToBitmapAllocation()
        }

    fun initialize() {
        bitmapScript = ScriptC_bitmap(rs)
        interpolateGapsScript = ScriptC_interpolate_gaps(rs)
        paletteUpdater = PaletteToScriptUpdater(rs, bitmapScript!!)
        bindToBitmapAllocation()
        isInitialized = true
    }

    private fun bindToBitmapAllocation() {
        if(bitmapScript == null) {
            // it will be initialized later.
            return
        }

        with(bitmapAllocation) {
            bitmapScript!!.bind_bitmapData(calcData)

            bitmapScript!!._width = width.toLong()
            bitmapScript!!._height = height.toLong()

            interpolateGapsScript!!._width = width.toLong()
            interpolateGapsScript!!._height = height.toLong()

            interpolateGapsScript!!._bitmap = rsBitmap
        }
    }

    fun scheduleBitmapUpdate() {
        bitmapUpdater.scheduleUpdate()
    }

    fun startAnimation(maxResolution: Int) {
        bitmapUpdater.maxAnimationResolution = maxResolution
        bitmapUpdater.isAnimation = true
        bitmapUpdater.scheduleUpdate()
    }

    fun stopAnimation() {
        bitmapUpdater.isAnimation = false
        // if the animation is running, an update is scheduled anyways.
    }

    /**
     * Renders bitmapData into the bitmap using the current parameters.
     * @param isLowRes Use lower resolution and no super sampling
     * @param maxRes maximum resolution of the longer edge
     */
    private fun updateBitmap(isLowRes: Boolean, maxRes: Int) {
        if(!isInitialized) {
            return
        }

        var currentPixelGap = bitmapAllocation.pixelGap

        if(isLowRes) {
            val size = max(bitmapAllocation.width, bitmapAllocation.height)

            while (size > maxRes * currentPixelGap) {
                currentPixelGap *= 2
            }
        }

        Log.d("BitmapController", "pixelGap = $currentPixelGap")

        bitmapScript!!._pixelGap = currentPixelGap.toLong()

        if(currentPixelGap == 1) {
            if(isLowRes) {
                bitmapScript!!.forEach_fastRoot(bitmapAllocation.rsBitmap)
            } else {
                bitmapScript!!.forEach_root(bitmapAllocation.rsBitmap)
            }
        } else {
            interpolateGapsScript!!._pixelGap = currentPixelGap.toLong()

            bitmapScript!!.forEach_fastRoot(bitmapAllocation.rsBitmap)
            interpolateGapsScript!!.forEach_root(bitmapAllocation.rsBitmap)
            rs.finish()
        }

        bitmapAllocation.sync()
    }

    fun updatePalettes(props: FractProperties) {
        if(paletteUpdater == null) {
            return
        }

        paletteUpdater!!.updatePalettes(props)
    }

    fun updateShaderProperties(props: FractProperties) {
        if(bitmapScript == null) {
            return
        }

        with(props) {
            bitmapScript!!._useLightEffect = if (shaderProperties.useLightEffect) 1 else 0
            bitmapScript!!._lightVector = shaderProperties.lightVector
            bitmapScript!!._ambientReflection = shaderProperties.ambientReflection
            bitmapScript!!._diffuseReflection = shaderProperties.diffuseReflection
            bitmapScript!!._specularReflection = shaderProperties.specularReflection
            bitmapScript!!._shininess = shaderProperties.shininess.toLong()
        }
    }

    interface Listener {
        fun bitmapUpdated()
    }

    private inner class BitmapUpdater: Runnable {
        var maxAnimationResolution: Int = -1
        var isAnimation: Boolean = false
        private var isLastAnimationFrame = false

        var isUpdateScheduled: Boolean = false
        var lastUpdateTimestamp: Long = -1
        var handler: Handler = Handler()

        fun scheduleUpdate() {
            if(isUpdateScheduled) {
                return
            }

            isUpdateScheduled = true
            updateDelayed()
        }

        fun updateDelayed() {
            val delay = minDelayBetweenUpdatesMs - (System.currentTimeMillis() - lastUpdateTimestamp)
            handler.postDelayed(this, max(1L, delay))
        }

        override fun run() {
            //

            updateBitmap(isAnimation || isLastAnimationFrame, maxAnimationResolution)
            bitmapUpdater.lastUpdateTimestamp = System.currentTimeMillis()

            if(isAnimation || isLastAnimationFrame) {
                isLastAnimationFrame = isAnimation
                updateDelayed()
            } else {
                bitmapUpdater.isUpdateScheduled = false
            }

            listener?.bitmapUpdated()
        }
    }

    companion object {
        private const val minDelayBetweenUpdatesMs = 25 // max 40 frames/sec
    }
}