package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sqrt

class Arc(val a: Cplx, val b: Cplx, val r: Double): Segment {
    private fun getCenter(): Cplx {
        val q2 = (a.re() - b.re()) * (a.re() - b.re()) + (a.im() - b.im()) * (a.im() - b.im())
        val d = sqrt(r * r / q2 - 0.25)

        val mx = (a.re() + b.re()) / 2.0
        val my = (a.im() + b.im()) / 2.0

        return Cplx(
                mx + (b.im() - a.im()) * d,
                my + (a.re() - b.re()) * d)
    }

    override fun dist(q: Cplx): Double {
        val c = getCenter()

        val isInBetween = isLeftOf(a, c, q) && isLeftOf(c, b, q)
        return if(isInBetween xor (r > 0.0)) {
            min(hypot(a.re() - q.re(), a.im() - q.im()), hypot(b.re() - q.re(), b.im() - q.im()))
        } else {
            abs(hypot(q.re() - c.re(), q.im() - c.im()) - abs(r))
        }
    }

    companion object {
        /**
         * Consider that the orientation of the coordinate system that positive y is facing up.
         */
        fun isLeftOf(a: Cplx, b: Cplx, q: Cplx): Boolean {
            return (b.im() - a.im()) * (q.re() - a.re()) < (b.re() - a.re()) * (q.im() - a.im())
        }
    }
}