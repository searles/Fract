package at.searles.commons.color

import kotlin.math.max
import kotlin.math.min

object Colors {
    fun brightness(rgb: Int): Float {
        val r = rgb and 0x00ff0000 shr 16
        val g = rgb and 0x0000ff00 shr 8
        val b = rgb and 0x000000ff
        return (0.299f * r + 0.587f * g + 0.114f * b) / 255f
    }

    fun toColorString(color: Int): String {
        return if (color and 0xff000000.toInt() == 0xff000000.toInt()) { // alpha is 100%
            String.format("#%06x", color and 0xffffff)
        } else {
            String.format("#%08x", color)
        }
    }

    private fun toHexDigit(digit: Char): Int {
        if (digit in '0'..'9') return digit - '0' else if (digit in 'A'..'F') return digit - 'A' + 10 else if (digit in 'a'..'f') return digit - 'a' + 10
        return -1
    }

    fun fromColorString(colorString: String): Int {
        var color = colorString
        if (color.startsWith("#")) {
            color = color.substring(1)
            var c = -0x1
            if (color.length == 3 || color.length == 4) {
                for (element in color) {
                    val d =
                        toHexDigit(element)
                    if (d == -1) throw ColorFormatException(
                            "bad format: $color"
                    )
                    c = c shl 4
                    c = c or d
                    c = c shl 4
                    c = c or d
                }
                return c
            } else if (color.length == 6 || color.length == 8) {
                for (element in color) {
                    val d =
                        toHexDigit(element)
                    if (d == -1) throw ColorFormatException(
                            "bad format: $color"
                    )
                    c = c shl 4
                    c = c or d
                }
                return c
            }
        }
        throw ColorFormatException("bad format: $color")
    }

    fun argb(red: Int, green: Int, blue: Int, alpha: Int = 0xff): Int {
        val r = clamp(red)
        val g = clamp(green)
        val b = clamp(blue)
        val a = clamp(alpha)
        return a shl 24 or (r shl 16) or (g shl 8) or b
    }

    fun red(argb: Int): Int {
        return (argb shr 16 and 0x0ff)
    }

    fun green(argb: Int): Int {
        return (argb shr 8 and 0x0ff)
    }

    fun blue(argb: Int): Int {
        return (argb and 0x0ff)
    }

    fun alpha(argb: Int): Int {
        return (argb shr 24 and 0x0ff)
    }

    private fun clamp(x: Int): Int {
        return max(0, min(255, x))
    }

    /**
     * @param alpha In range 0f..1f
     */
    fun transparent(alpha: Float, color: Int): Int {
        val newAlpha = min(255, max(0, (alpha(color) * alpha + 0.5f).toInt()))
        return newAlpha.shl(24).or(color.and(0xffffff))
    }

    fun toGray(color: Int): Int {
        val brightness = min(255, (brightness(color) * 256f).toInt())
        return argb(brightness, brightness, brightness, alpha(color))
    }
}