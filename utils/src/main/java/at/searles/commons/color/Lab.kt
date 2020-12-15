package at.searles.commons.color

import kotlin.math.pow

data class Lab(val l: Float, val a: Float, val b: Float, val alpha: Float = 1f) {
    fun toRgb(): Rgb {
        val fx = (l + 16.0) / 116.0 + a / 500.0
        val fy = (l + 16.0) / 116.0
        val fz = (l + 16.0) / 116.0 - b / 200.0
        val X = 0.9505 * fInverse(fx)
        val Y = 1.0 * fInverse(fy)
        val Z = 1.0890 * fInverse(fz)
        val r0 = X * 3.2404542 - Y * 1.5371385 - Z * 0.4985314
        val g0 = -X * 0.9692660 + Y * 1.8760108 + Z * 0.0415560
        val b0 = X * 0.0556434 - Y * 0.2040259 + Z * 1.0572252

        return Rgb(
            K(r0).toFloat(),
            K(g0).toFloat(),
            K(b0).toFloat(),
            alpha
        )
    }

    private fun fInverse(t: Double): Double {
        return if (t > 648.0 / 3132.0) t * t * t else (116.0 * t - 16.0) * 27.0 / 24389.0
    }

    private fun K(g: Double): Double {
        return if (g > 0.0031308) {
            1.055 * g.pow(1.0 / 2.4) - 0.055
        } else {
            12.92 * g
        }
    }
}