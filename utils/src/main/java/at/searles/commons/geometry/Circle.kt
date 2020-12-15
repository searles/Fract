package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import kotlin.math.abs

class Circle(val c: Cplx, val radius: Double): Segment {
    override fun dist(q: Cplx): Double {
        return abs(Cplx().sub(c, q).rad() - radius)
    }
}

/*
static double circle(double2 mid, double r, double2 p) {
	return abs(r - rad(p - mid));
}

 */