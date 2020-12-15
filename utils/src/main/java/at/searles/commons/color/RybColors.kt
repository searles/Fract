package at.searles.commons.color

import at.searles.commons.color.Commons.inRange
import at.searles.commons.color.Commons.rangeFraction
import kotlin.math.floor

/**
 * special color model with yellow as own component.
 */
object RybColors {
    private val keyColors = arrayOf(
        Rgb(1.00f, 0.00f, 0.00f), // * Red
        Rgb(1.00f, 0.25f, 0.00f), // *** Vermilion
        Rgb(1.00f, 0.50f, 0.00f), // ** Orange
        Rgb(1.00f, 0.75f, 0.00f), // *** Amber
        Rgb(1.00f, 1.00f, 0.00f), // * Yellow
        Rgb(0.50f, 1.00f, 0.00f), // *** Chartreuse
        Rgb(0.00f, 1.00f, 0.00f), // ** Green
        Rgb(0.00f, 1.00f, 1.00f), // *** Aqua
        Rgb(0.00f, 0.00f, 1.00f), // * Blue
        Rgb(0.50f, 0.00f, 1.00f), // *** Violet
        Rgb(1.00f, 0.00f, 1.00f), // ** Purple
        Rgb(1.00f, 0.00f, 0.50f) // *** Magenta
    )

    fun colorSegmentsCount(): Int {
        return keyColors.size
    }

    fun red(hue: Float): Float {
        val hue = hue - floor(hue)

        val i0 = (hue * keyColors.size).toInt()
        val fraction = hue * keyColors.size - i0

        val i1 = (i0 + 1) % keyColors.size

        return keyColors[i0].red * (1 - fraction) + keyColors[i1].red * fraction
    }

    fun green(hue: Float): Float {
        var hue = hue - floor(hue)

        val i0 = (hue * keyColors.size).toInt()
        val fraction = hue * keyColors.size - i0

        val i1 = (i0 + 1) % keyColors.size

        return keyColors[i0].green * (1 - fraction) + keyColors[i1].green * fraction
    }

    fun blue(hue: Float): Float {
        val hue = hue - floor(hue)

        val i0 = (hue * keyColors.size).toInt()
        val fraction = hue * keyColors.size - i0

        val i1 = (i0 + 1) % keyColors.size

        return keyColors[i0].blue * (1 - fraction) + keyColors[i1].blue * fraction
    }

    fun color(hue: Float): Rgb {
        val hue = hue - floor(hue)

        var i0 = (hue * keyColors.size).toInt()
        val fraction = hue * keyColors.size - i0

        i0 %= keyColors.size
        val i1 = (i0 + 1) % keyColors.size

        return Rgb(
                (keyColors[i0].red * (1 - fraction) + keyColors[i1].red * fraction),
                (keyColors[i0].green * (1 - fraction) + keyColors[i1].green * fraction),
                (keyColors[i0].blue * (1 - fraction) + keyColors[i1].blue * fraction)
        )
    }

    fun hue(r: Float, g: Float, b: Float): Float {
        var r = r
        var g = g
        var b = b
        val max = Math.max(r, Math.max(g, b))
        val min = Math.min(r, Math.min(g, b))

        if (max - min == 0f) {
            // gray
            return 0f
        }

        // increase contrast
        r = (r - min) / (max - min)
        g = (g - min) / (max - min)
        b = (b - min) / (max - min)

        // assert((r == 1f || g == 1f || b == 1f) && (r == 0f || g == 0f || b == 0f))

        // assert one is 1 and another one is 0.

        var i0 = 0
        var i1: Int

        while (true) {
            i1 = (i0 + 1) % keyColors.size

            if (inRange(r, keyColors[i0].red, keyColors[i1].red) && inRange(
                    g,
                    keyColors[i0].green,
                    keyColors[i1].green
                ) && inRange(b, keyColors[i0].blue, keyColors[i1].blue)
            ) {
                break
            }

            if (++i0 >= keyColors.size) {
                throw IllegalArgumentException()
            }
        }

        val fraction = Math.max(
            Math.max(
                rangeFraction(r, keyColors[i0].red, keyColors[i1].red),
                rangeFraction(g, keyColors[i0].green, keyColors[i1].green)
            ),
            rangeFraction(b, keyColors[i0].blue, keyColors[i1].blue)
        )

        return (i0 + fraction) / keyColors.size
    }

    /*private fun inRange(x: Float, a: Float, b: Float): Boolean {
        var a = a
        var b = b
        // Convenience from at.searles.math
        if (b < a) {
            val t = b
            b = a
            a = t
        }

        return a <= x && x <= b
    }

    private fun rangeFraction(x: Float, a: Float, b: Float): Float {
        // Convenience from at.searles.math
        return if (b - a == 0f) {
            0f
        } else (x - a) / (b - a)
    }*/
}
