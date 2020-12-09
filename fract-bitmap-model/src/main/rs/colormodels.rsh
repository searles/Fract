static float finv(float t) {
    return (t > 648.0 / 3132.0) ?
            t * t * t : (116.0 * t - 16.0) * 27.0 / 24389.0;
}

static float K(float g) {
    if(g > 0.0031308f) {
        return 1.055f * native_powr(g, 1.f / 2.4f) - 0.055f;
    } else {
        return 12.92f * g;
    }
}

static float4 labToRgb(float4 lab) {
    float fx = (lab.s0 + 16.f) / 116.f + lab.s1 / 500.f;
    float fy = (lab.s0 + 16.f) / 116.f;
    float fz = (lab.s0 + 16.f) / 116.f - lab.s2 / 200.f;

    float X = 0.9505f * finv(fx);
    float Y = 1.f * finv(fy);
    float Z = 1.0890f * finv(fz);

    float r0 = X * 3.2404542f - Y * 1.5371385f - Z * 0.4985314f;
    float g0 = -X * 0.9692660f + Y * 1.8760108f + Z * 0.0415560f;
    float b0 = X * 0.0556434f - Y * 0.2040259f + Z * 1.0572252f;

    return (float4) {K(r0), K(g0), K(b0), lab.s3};
}

static float4 rgbToYuv(float4 rgb) {
    float y = 0.299f * rgb.s0 + 0.587f * rgb.s1 + 0.114f * rgb.s2;
    float u = -0.14713 * rgb.s0 - 0.28886f * rgb.s1 + 0.436f * rgb.s2;
    float v = 0.615 * rgb.s0 - 0.51499f * rgb.s1 - 0.10001f * rgb.s2;

    return (float4) {y, u, v, rgb.s3};
}

static float4 yuvToRgb(float4 yuv) {
    float r = yuv.s0 + 1.13983f * yuv.s2;
    float g = yuv.s0 - 0.39465f * yuv.s1 - 0.58060f * yuv.s2;
    float b = yuv.s0 + 2.03211f * yuv.s1;

    return (float4) {r, g, b, yuv.s3};
}

