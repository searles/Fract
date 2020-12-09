package at.searles.fractimageview

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.math.min

abstract class ScalableBitmapModel {

    val width
        get() = bitmap.width
    val height
        get() = bitmap.height

    /**
     * The current bitmap to be drawn
     */
    abstract val bitmap: Bitmap

    /**
     * A matrix that should be used to transform the bitmap before drawing it.
     * Needed eg if the bitmap should be scale but no new image data is available yet.
     */
    abstract val bitmapTransformMatrix: Matrix

    /**
     * Registers a scale event. The matrix itself is normalized, ie, (0,0) is the center
     * and the image is supposed to fully fix the square (-1,-1)-(1,1) (left-up to right-down)
     */
    abstract fun scale(relativeMatrix: Matrix)

    /**
     * Inverse of bitmap2norm
     */
    fun normToBitmapMatrix(ret: Matrix = Matrix()): Matrix {
        val m = min(width, height)
        return ret.apply {
            setValues(floatArrayOf(
                m / 2f, 0f, width / 2f,
                0f, m / 2f, height / 2f,
                0f, 0f, 1f
            ))
        }
    }

    /**
     * Matrices to convert coordinates into value that is
     * independent from the bitmap-size. Normalized always
     * contains the square -1,-1 - 1-1 with 0,0 in the middle
     * but also keeps the ratio of the image.
     */
    fun bitmapToNormMatrix(ret: Matrix = Matrix()): Matrix {
        val m = min(width, height).toFloat()
        return ret.apply {
            setValues(floatArrayOf(
                2f / m, 0f, -width / m,
                0f, 2f / m, -height / m,
                0f, 0f, 1f
            ))
        }
    }
}