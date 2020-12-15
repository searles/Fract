package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import kotlin.math.abs
import kotlin.math.min

class Line(val a: Cplx, val b: Cplx): Segment {

    private fun dot(a: Cplx, b: Cplx): Double {
        return (a.re() * b.re()) + (a.im() * b.im())
    }

    override fun dist(q: Cplx): Double {
        val dab = Cplx().sub(a, b).abs()
        val dap = Cplx().sub(a, q).abs()
        val dbq = Cplx().sub(b, q).abs()

        if(dot(Cplx().sub(a, b), Cplx().sub(q, b)) * dot(Cplx().sub(b, a), Cplx().sub(q, a)) >= 0) {
            val det = a.re() * b.im() + b.re() * q.im() + q.re() * a.im() -
                    q.re() * b.im() - b.re() * a.im() - a.re() * q.im()

            return abs(det) / dab
        } else {
            return min(dap, dbq)
        }
    }
}

/*
static double line(double2 p1, double2 p2, double2 p) {
	double a = p2.y - p1.y;
	double b = p1.x - p2.x;
	double c = p2.x * p1.y - p2.y * p1.x;
	double len = sqrt(a * a + b * b);
	return abs(a * p.x + b * p.y + c) / len;
}

static double segment(double2 a, double2 b, double2 p) {
    // see http://stackoverflow.com/questions/25800286/how-to-get-the-point-to-line-segment-distance-in-2d
    double dab = rad(sub(a, b));
    double dap = rad(sub(a, p));
    double dbp = rad(sub(b, p));

	if(dot(sub(a, b), sub(p, b)) * dot(sub(b, a), sub(p, a)) >= 0) {
	    double det = a.x * b.y + b.x * p.y + p.x * a.y -
	    	p.x * b.y - b.x * a.y - a.x * p.y;

	    return abs(det) / dab;
	} else {
	    return min(dap, dbp);
	}
}

 */