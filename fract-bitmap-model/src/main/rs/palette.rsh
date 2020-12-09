// structures cannot contain pointers in renderscript
typedef struct palette {
    uint32_t segmentIndex; 

    uint32_t w;
    uint32_t h;
    float offsetX;
    float offsetY;
} palette_t;

typedef struct paletteSegment {
    rs_matrix4x4 comp0;
    rs_matrix4x4 comp1;
    rs_matrix4x4 comp2;
    rs_matrix4x4 alpha;
} segment_t;

uint32_t palettesCount;
palette_t *palettes;
segment_t *segments;

// helper function for the palette:
static float z(rs_matrix4x4 * m, float2 c) {
    // assert 0 <= c.x <= 1
    // assert 0 <= c.y <= 1
    return (((((((m->m[15] * c.y) + m->m[11]) * c.y + m->m[7]) * c.y) + m->m[3]) * c.x +
            (((((m->m[14] * c.y) + m->m[10]) * c.y + m->m[6]) * c.y) + m->m[2])) * c.x +
            (((((m->m[13] * c.y) + m->m[9]) * c.y + m->m[5]) * c.y) + m->m[1])) * c.x +
            (((((m->m[12] * c.y) + m->m[8]) * c.y + m->m[4]) * c.y) + m->m[0]);
}

static uint32_t pmod(float x, uint32_t divisor) {
    // positive modulo
    uint32_t x0;

    if(x < 0) {
        x0 = (uint32_t) -x;
        x0 %= divisor;
        if(x0 != 0) {
            x0 = divisor - x0;
        }
    } else  {
        x0 = (uint32_t) x;
        x0 %= divisor;
    }

    return x0;
}

/*
 * precondition: 0 <= x < 1; 0 <= y < 1
 */
static float4 colorAtNormalized(int layer, float2 pt) {
    layer = layer % palettesCount;

    if(layer < 0) {
        layer = layer + palettesCount;
    }

    struct palette *p = &(palettes[layer]);

    pt.x = (pt.x + p->offsetX) * p->w;
    pt.y = (pt.y + p->offsetY) * p->h;

    float2 pt0 = floor(pt);
    pt = pt - pt0;

    uint32_t x0 = pmod(pt0.x, p->w);
    uint32_t y0 = pmod(pt0.y, p->h);

    // determine which palette to use
    uint32_t segmentIndex = p->segmentIndex + (x0 + y0 * p->w);

    segment_t segment = segments[segmentIndex];

    return (float4) {
        z(&(segment.comp0), pt),
        z(&(segment.comp1), pt),
        z(&(segment.comp2), pt),
        z(&(segment.alpha), pt)
    };
}

static float4 colorAt(float x, float y) {
    int layer = (int) floor(x);

    x = x - floor(x);

    return colorAtNormalized(layer, (float2) {x, y});
}