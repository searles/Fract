#include "complex.rsh"

static double circle(double2 c, double radius, double2 q) {
    return abs(abs(q - c) - radius);
}

static double line(double2 a, double2 b, double2 p) {
    // see http://stackoverflow.com/questions/25800286/how-to-get-the-point-to-line-segment-distance-in-2d
    double dap = abs(a - p);
    double dbp = abs(b - p);

	if(dot(a - b, p - b) * dot(b - a, p - a) >= 0) {
        double dab = abs(a - b);

	    double det = a.x * b.y + b.x * p.y + p.x * a.y -
	    	p.x * b.y - b.x * a.y - a.x * p.y;

	    return abs(det) / dab;
	} else {
	    return min(dap, dbp);
	}
}

static int isLeftOf(double2 a, double2 b, double2 q) {
   return (b.y - a.y) * (q.x - a.x) < (b.x - a.x) * (q.y - a.y);
}

static double arc(double2 a, double2 b, double r, double2 q) {
    double q2 = (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
    double d = sqrt(r * r / q2 - 0.25);

    double2 m = (a + b) / 2.0;

    // center of the circle
    double2 c = (double2) {
            m.x + (b.y - a.y) * d,
            m.y + (a.x - b.x) * d
    };

    int isInBetween = isLeftOf(a, c, q) && isLeftOf(c, b, q);

    if(isInBetween ^ (r > 0.0)) {
        // closer to a corner
        return min(abs(a - q), abs(b - q));
    } else {
        // closer to the arc.
        return abs(abs(q - c) - abs(r));
    }
}

static double rect(double2 a, double2 b, double2 q) {
    double2 d0 = min(a, b) - q;
    double2 d1 = q - max(a, b);

    double2 d = max(d0, d1);

    if(d.x < 0 && d.y < 0) {
        return -max(d.x, d.y);
    }

    if(d.x < 0) {
        return d.y;
    }

    if(d.y < 0) {
        return d.x;
    }

    return abs(d);
}