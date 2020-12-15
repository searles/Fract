package at.searles.commons.color

import at.searles.commons.util.IntIntMap
import kotlin.math.exp

class Palette(val width: Int, val height: Int, val offsetX: Float, val offsetY: Float, val colorPoints: IntIntMap<Lab>) {
    val colorTable: Array<Array<Lab>> by lazy {
        createColorTable()
    }

    fun createColorTable(): Array<Array<Lab>> {
        return Array(height) {y ->
            Array(width) {x ->
                calculateColorAt(x, y)
            }
        }
    }

    private fun isColorPoint(x: Int, y: Int): Boolean {
        return colorPoints.containsKey(x, y)
    }

    private fun calculateColorAt(x: Int, y: Int): Lab {
        if(isColorPoint(x, y)) {
            return colorPoints.getValue(x, y)
        }

        var sumWeights = 0.0
        var l = 0.0
        var a = 0.0
        var b = 0.0
        var alpha = 0.0

        val iterator = colorPoints.iterator()

        while(iterator.hasNext()) {
            val entry = iterator.next()

            if(entry.x >= width || entry.y >= height) {
                continue
            }

            val weight = getWeight(entry.x, entry.y, x, y)
            sumWeights += weight

            val lab = entry.value

            alpha += lab.alpha * weight
            l += lab.l * weight
            a += lab.a * weight
            b += lab.b * weight

        }

        return Lab((l / sumWeights).toFloat(), (a / sumWeights).toFloat(), (b / sumWeights).toFloat(), (alpha / sumWeights).toFloat())
    }

    private fun getWeight(ptX: Int, ptY: Int, col: Int, row: Int): Double {
        // The weight uses a round-about.
        var weight = 0.0

        (-1..1).forEach { yAdd ->
            (-1..1).forEach { xAdd ->
                weight += getSimpleWeight(ptX + width * xAdd, ptY + height * yAdd, col, row)
            }
        }

        return weight
    }

    private fun getSimpleWeight(ptX: Int, ptY: Int, col: Int, row: Int): Double {
        val dx = (ptX - col).toDouble() / width
        val dy = (ptY - row).toDouble() / height

        if(dx < -1.0 || 1.0 <= dx || dy < -1.0 || 1.0 <= dy) {
            return 0.0
        }

        val dist2 = (dx * dx + dy * dy)

        return 1.0 / (exp(weightExpFactor * dist2) - 1)
    }

    companion object {
        private const val weightExpFactor = 12.0
    }
}