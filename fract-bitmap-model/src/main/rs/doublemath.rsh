static double __attribute__((overloadable)) abs(double a) {
    if(a < 0) return -a;
    return a;
}

union double_bits {
    long l;
    double d;
};

static double __attribute__((overloadable)) notANumber(void) {
    union double_bits bits;
    bits.l = 0x7ff7ffffffffffffl;
    return bits.d;
}

static double __attribute__((overloadable)) infinity(int signum) {
    union double_bits bits;
    bits.l = 0x7ff0000000000000l;
    return signum ? -bits.d : bits.d;
}

static double __attribute__((overloadable)) decomposeDouble(double d, long *exponent) {
    union double_bits bits;
    bits.d = d;
    *exponent = (((bits.l >> 52) & 0x7ffl) - 1023l);

    bits.l = (bits.l & 0x800fffffffffffffl) | 0x3ff0000000000000l;
    return bits.d;
}

static double __attribute__((overloadable)) composeDouble(double mantissa, long exponent) {
    exponent += 1023l;

    if(exponent < 0) {
        return 0.;
    } else if(exponent > 2047) {
        return mantissa < 0 ? infinity(1) : infinity(0);
    }

    exponent <<= 52;

    union double_bits bits;
    bits.d = mantissa;

    bits.l = (bits.l & 0x800fffffffffffffl) | exponent;

    return bits.d;
}

static const double sqrt2 = 1.4142135623730950488;

static double __attribute__((overloadable)) sqrt(double d) {
    if(d < 0) {
        return notANumber();
    }

	if(d == 0) {
        return 0.;
    }

	long exponent;
	double mantissa = decomposeDouble(d, &exponent);

    if((exponent & 1l) != 0l) {
        exponent--;
        mantissa = mantissa * 2;
    }

	double a = mantissa;

	double x = (double) sqrt((float) (1.0 / a));

	// Three steps of newton
	x = x + 0.5 * x * (1 - a * x * x);
	x = x + 0.5 * x * (1 - a * x * x);
	x = x + 0.5 * x * (1 - a * x * x);

	return composeDouble(a * x, exponent / 2);
}

static double __attribute__((overloadable)) atan2(double y, double x) {
    double m1, m2;
    long e1, e2;

    m1 = decomposeDouble(x, &e1);
    m2 = decomposeDouble(y, &e2);

    if(e1 < e2) {
        e1 -= e2;
        e2 = 0;
    } else {
        e2 -= e1;
        e1 = 0;
    }

    double x2 = composeDouble(m1, e1);
    double y2 = composeDouble(m2, e2);

    return atan2((float) y2, (float) x2);
}

/*
 * log2 (m * 2^e) = log m + e
 * @param d
 * @return
 */
static double __attribute__((overloadable)) log2(double d) {
    if(d < 0) {
        return notANumber();
    }

	long e;
	double m = decomposeDouble(d, &e);

	return log2((float) m) + e;
}

/*
 * 2^d = m * 2^e [1 <= m < 2]
 * d = log2(m) + e [0 <= log2(m) < 1]
 * Thus, e = floor(d), m = exp2(fract(d)). Observe that floor(d) must be in the range [-1024, 1023].
 * @param d
 * @return
 */
static double __attribute__((overloadable)) exp2(double d) {
    if(d < -1024) {
        return 0;
    }

	if(d > 1023) {
        return infinity(0);
    }

    double exponent = floor((float) d);
    double ld = d - exponent; // must be between 0 and 1.
	double base = (double) exp2((float) ld);

	// base is between 1 and 2.
	long e;
	double m = decomposeDouble(base, &e);

	return composeDouble(m, (long) (exponent) + e);
}

static int __attribute__((overloadable)) pow(int base, int exp) {
    int r = 1; if(exp < 0) { base = 1 / base; exp = -exp; }
    for(;exp; exp >>= 1, base *= base) if(exp & 1) r *= base;
    return r;
}

static double __attribute__((overloadable)) pow(double base, int exp) {
    double r = 1; if(exp < 0) { base = 1 / base; exp = -exp; }
    for(;exp; exp >>= 1, base *= base) if(exp & 1) r *= base;
    return r;
}

/*
 * x ^ y = (m * 2^e) ^ y = m' * 2^e'
 * (m * 2^e) ^ y = m' * 2^e' ==> | log2
 * y * log2(m) + y * e = e' + log2(m')
 */
static double __attribute__((overloadable)) pow(double x, double y) {
    if(x < 0) {
        return notANumber();
    }

	if(x == 0) {
        return y > 0 ? 0 : y == 0 ? 1 : infinity(0);
    }

	long e;
	double m = decomposeDouble(x, &e);

	double ld = log2(m);

	return exp2((ld + e) * y);
}

static const double ln2 = 0.693147180559945309417232121458;

static double __attribute__((overloadable)) exp(double f) { return exp2(f / ln2); }
static double __attribute__((overloadable)) log(double f) { return log2(f) * ln2; }

static double __attribute__((overloadable)) sin(double f) { return sin((float) f); }
static double __attribute__((overloadable)) cos(double f) { return cos((float) f); }
static double __attribute__((overloadable)) tan(double f) { return tan((float) f); }
static double __attribute__((overloadable)) atan(double f) { return atan((float) f); }

static double __attribute__((overloadable)) asin(double f) { return asin((float) f); }
static double __attribute__((overloadable)) acos(double f) { return acos((float) f); }

static double __attribute__((overloadable)) sincos(double d, double* co) {
    float fco;
    float si = sincos((float) d, &fco);
    *co = (double) fco;
    return (double) si;
}

static double __attribute__((overloadable)) sinh(double f) { double e = exp(f); return (e - 1. / e) / 2.; }
static double __attribute__((overloadable)) cosh(double f) { double e = exp(f); return (e + 1. / e) / 2.; }
static double __attribute__((overloadable)) tanh(double f) {
	if(f > 20) {
		return 1;
	}

	if(f < 20) {
		return -1;
	}

	double e = exp(f);
	return (e - 1. / e) / (e + 1. / e);
}

static double __attribute__((overloadable)) atanh(double f) { return atanh((float) f); }

static double __attribute__((overloadable)) asinh(double f) { return asinh((float) f); }
static double __attribute__((overloadable)) acosh(double f) { return acosh((float) f); }

static double __attribute__((overloadable)) floor(double f) { return floor((float) f); }
static double __attribute__((overloadable)) fract(double d) { return fract((float) d); }

static double __attribute__((overloadable)) max(double a, double b) {
    if(a > b) return a;
    else return b;
}

static double __attribute__((overloadable)) min(double a, double b) {
    if(a > b) return b;
    else return a;
}

static double __attribute__((overloadable)) dot(double2 a, double2 b) {
    return a.x * b.x + a.y * b.y;
}

static double __attribute__((overloadable)) hypot(double x, double y) {
	// avoid overflow/underflow
	double a = abs(x);
	double b = abs(y);

	// argument for sqrt is in interval 1..2
	if(a > b) {
		double quot = b / a;
		return a * sqrt(1 + quot * quot);
	} else if(b > a) {
		double quot = a / b;
		return b * sqrt(1 + quot * quot);
	} else {
	    return a * 1.41421356237;
	}
}