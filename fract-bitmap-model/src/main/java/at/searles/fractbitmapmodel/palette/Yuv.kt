package at.searles.fractbitmapmodel.palette

import at.searles.commons.color.Rgb

class Yuv(val y: Float, val u: Float, val v: Float, val alpha: Float = 1f) {
    constructor(rgb: Rgb): this(
        0.299f * rgb.red + 0.587f * rgb.green + 0.114f * rgb.blue,
        -0.14713f * rgb.red - 0.28886f * rgb.green + 0.436f * rgb.blue,
        0.615f * rgb.red - 0.51499f * rgb.green - 0.10001f * rgb.blue,
        rgb.alpha
        )
}