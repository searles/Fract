package at.searles.commons.color

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class Rgb(val red: Float, val green: Float, val blue: Float, val alpha: Float = 1f) {

    private fun f(t: Double): Double {
        return if (t > 216.0 / 24389.0) Math.cbrt(t) else (24389.0 / 27.0 * t + 16.0) / 116.0//841.0 / 108.0 * t + 4.0 / 29.0
    }

    private fun g(K: Double): Double {
        return if (K > 0.04045) {
            ((K + 0.055) / 1.055).pow(2.4)
        } else {
            K / 12.92
        }
    }

    fun toLab(): Lab {
        val r0 = g(red.toDouble())
        val g0 = g(green.toDouble())
        val b0 = g(blue.toDouble())
        // see http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
        val X = 0.4124564 * r0 + 0.3575761 * g0 + 0.1804375 * b0
        val Y = 0.2126729 * r0 + 0.7151522 * g0 + 0.0721750 * b0
        val Z = 0.0193339 * r0 + 0.1191920 * g0 + 0.9503041 * b0
        val fx = f(X / 0.9505)
        val fy = f(Y / 1.0)
        val fz = f(Z / 1.0890)

        return Lab(
            (116.0 * fy - 16).toFloat(),
            (500.0 * (fx - fy)).toFloat(),
            (200.0 * (fy - fz)).toFloat(), alpha
        )
    }

    fun toArgb(): Int {
        val r = clamp255(red)
        val g = clamp255(green)
        val b = clamp255(blue)
        val a = clamp255(alpha)
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    private fun clamp255(f: Float): Int {
        return max(0f, min(f * 256f, 255f)).toInt()
    }

    companion object {
        fun of(argb: Int): Rgb {
            val red = (argb shr 16 and 0x0ff) / 255.0f
            val green = (argb shr 8 and 0x0ff) / 255.0f
            val blue = (argb and 0x0ff) / 255.0f
            val alpha = (argb shr 24 and 0x0ff) / 255.0f
            return Rgb(red, green, blue, alpha)
        }
    }

}