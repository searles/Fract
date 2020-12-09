package at.searles.fractimageview.demo

import android.graphics.Bitmap
import android.graphics.Matrix
import at.searles.fractimageview.ScalableBitmapModel

class DemoBitmapProvider: ScalableBitmapModel() {

    private val scaleMatrix = Matrix()

    override var bitmap: Bitmap =
        Bitmap.createBitmap(50, 10, Bitmap.Config.ARGB_8888).apply {
            eraseColor(android.graphics.Color.GREEN)
        }

    override val bitmapTransformMatrix: Matrix
        get() = scaleMatrix

    override fun scale(relativeMatrix: Matrix) {
        scaleMatrix.postConcat(relativeMatrix)
    }
}