#include "doublemath.rsh"

static double2 __attribute__((overloadable)) mul(double2 a, double2 b) {
    return (double2){ a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x };
}

static double2 __attribute__((overloadable)) div(double2 a, double2 b) {
    double r = b.x * b.x + b.y * b.y;
    return (double2){ (a.x * b.x + a.y * b.y) / r, (-a.x * b.y + a.y * b.x) / r };
}

static double2 __attribute__((overloadable)) rec(double2 a) {
    double r = a.x * a.x + a.y * a.y;
    return (double2){ a.x / r, -a.y / r };
}

static double __attribute__((overloadable)) abs(double2 z) {
    return hypot(z.x, z.y);
}

static double __attribute__((overloadable)) arg(double2 f) {
    return atan2(f.y, f.x);
}

static const double tau = 6.283185307179586;

static double __attribute__((overloadable)) argnorm(double2 f) {
    double a = arg(f) / tau;

    if(a < 0) {
        return 1 + a;
    }

    return a;
}

static double2 __attribute__((overloadable)) exp(double2 z) {
	double ez = exp(z.x);

	double si; // sin(z.y);
	double co; // cos(z.y);
	si = sincos(z.y, &co);

	double2 result = ez * (double2) {co, si};

	return result;
}


static double2 __attribute__((overloadable)) log(double2 z) {
	return (double2) { log(abs(z)), arg(z) };
}

static double2 __attribute__((overloadable)) pow(double2 base, int exp) {
    double2 r = (double2){1., 0.};
    if(exp < 0) { base = rec(base); exp = -exp; }
    for(;exp; exp >>= 1, base = mul(base, base))
        if(exp & 1) r = mul(r, base);
    return r;
}

static double2 __attribute__((overloadable)) pow(double2 base, double power) {
	if(base.x == 0 && base.y == 0) return (double2) {0, 0};

	double r = pow(abs(base), power);
	double pa = power * arg(base);

    double si, co;
    si = sincos(pa, &co);

	return r * (double2) {co, si};
}

static double2 __attribute__((overloadable)) pow(double2 base, double2 power) {
	if(base.x == 0 && base.y == 0) return (double2) {0, 0};

	double lrb = log(abs(base));
	double ab = arg(base);

	// = exp (lrb * pr - ab * pi, lrb * pi + ab * pr)
	double2 prod = (double2) {lrb * power.x - ab * power.y, lrb * power.y + ab * power.x};

	return exp(prod);
}

static double2 cabs(double2 a) {
    if(a.x < 0) a.x = -a.x;
    if(a.y < 0) a.y = -a.y;
    return a;
}

static double2 __attribute__((overloadable)) sqrt(double2 f) {
	double r = abs(f);
	double2 ret = { sqrt((r + f.x) / 2), sqrt((r - f.x) / 2) };
	if(f.y < 0) ret.y = -ret.y;
	return ret;
}

static double2 __attribute__((overloadable)) atan(double2 f) {
    double2 a = log(div((double2) {1.0 + f.y, -f.x}, (double2) {1.0 - f.y, f.x}));
    return (double2) {-a.y, a.x} / 2.0;
}

static double2 __attribute__((overloadable)) asin(double2 f) {
    // -i * log(iz + sqrt(1-z^2))
    double2 i = (double2) {0.0, 1.0};
    return mul(-i, log(mul(i, f) + sqrt((double2) {1.0, 0.0} - mul(f, f))));
}

static double2 __attribute__((overloadable)) acos(double2 f) {
    // -i * ln(z + sqrt(z^2 - 1))
    double2 i = (double2) {0.0, 1.0};
    return mul(-i, log(f + sqrt(mul(f, f) - (double2) {1.0, 0.0})));
}

static double2 __attribute__((overloadable)) conj(double2 f) {
	return (double2) {f.x, -f.y};
}

static double2 __attribute__((overloadable)) muli(double2 f) {
	return (double2) {-f.y, f.x};
}

static double2 __attribute__((overloadable)) rabs(double2 f) {
	return (double2) {abs(f.x), f.y};
}

static double2 __attribute__((overloadable)) iabs(double2 f) {
	return (double2) {f.x, abs(f.y)};
}

static double2 __attribute__((overloadable)) sinh(double2 z) {
	double2 ez = exp(z);
	return (ez - rec(ez)) / 2;
}

static double2 __attribute__((overloadable)) cosh(double2 z) {
	double2 ez = exp(z);
	return (ez + rec(ez)) / 2;
}


static double2 __attribute__((overloadable)) sin(double2 z) {
	return -muli(sinh(muli(z)));
}

static double2 __attribute__((overloadable)) cos(double2 z) {
	return cosh(muli(z));
}

static double2 __attribute__((overloadable)) fract(double2 z) {
	return (double2) { fract(z.x), fract(z.y) };
}

static double2 __attribute__((overloadable)) tan(double2 a) {
	// (e^2iz - 1) / (e^2iz + 1)
	double2 eia = exp((double2) {-2 * a.y, 2 * a.x});
	double2 eiai = (double2) {-eia.y, eia.x + 1};

    eia.x -= 1;

	return div(eia, eiai);
}

static double2 __attribute__((overloadable)) tanh(double2 a) {
	double2 ea = exp((double2) {2 * a.x, 2 * a.y});
	return div((double2){ea.x - 1, ea.y}, (double2){ea.x + 1, ea.y});
}

static double2 __attribute__((overloadable)) atanh(double2 a) {
   	double2 b = div((double2){1 + a.x, a.y}, (double2){1 - a.x, -a.y});
   	double2 c = log(b);
   	return (double2) { c.x / 2, c.y / 2};
}

static double2 __attribute__((overloadable)) asinh(double2 f) {
    // ln(z + sqrt(z^2 + 1))
    return log(f + sqrt((double2) {1.0, 0.0} + mul(f, f)));
}

static double2 __attribute__((overloadable)) acosh(double2 f) {
    // ln(z + sqrt(z^2 - 1))
    return log(f + sqrt(mul(f, f) - (double2) {1.0, 0.0}));
}

static double2 __attribute__((overloadable)) max(double2 a, double2 b) {
	return (double2) {max(a.x, b.x), max(a.y, b.y)};
}

static double2 __attribute__((overloadable)) min(double2 a, double2 b) {
	return (double2) {min(a.x, b.x), min(a.y, b.y)};
}

static double2 __attribute__((overloadable)) norm(double2 z) {
	return z / abs(z);
}

static double2 __attribute__((overloadable)) floor(double2 z) {
	return (double2) {floor(z.x), floor(z.y)};
}
