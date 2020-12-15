package at.searles.commons.geometry

import at.searles.commons.math.Cplx

interface Segment {
    fun dist(q: Cplx): Double

    fun dist(x: Double, y: Double): Double {
        return dist(Cplx(x, y))
    }
}