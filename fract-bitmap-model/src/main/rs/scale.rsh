typedef struct Scale {
    double a;
    double b;
    double c;
    double d;
    double e;
    double f;
} Scale_t;

Scale_t scale;

static double2 mapCoordinates(uint32_t x, uint32_t y) {
    return (double2) { scale.a * x + scale.b * y + scale.e, scale.c * x + scale.d * y + scale.f };
}
