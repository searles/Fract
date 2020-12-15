package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

class Rect(val a: Cplx, val b: Cplx): Segment {
    override fun dist(q: Cplx): Double {
        val minX = min(a.re(), b.re())
        val minY = min(a.im(), b.im())
        val maxX = max(a.re(), b.re())
        val maxY = max(a.im(), b.im())

        val dx = max(minX - q.re(), q.re() - maxX)
        val dy = max(minY - q.im(), q.im() - maxY)

        if(dx < 0 && dy < 0) {
            return -max(dx, dy)
        }

        if(dx < 0) {
            return dy
        }

        if(dy < 0) {
            return dx
        }

        return hypot(dx, dy)
    }
}