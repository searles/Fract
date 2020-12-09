/*
 * In bitmap, pixels with coordinates i * (pixelGap, pixelGap) are used to interpolate all other
 * values in between.
 */

#pragma version(1)
#pragma rs java_package_name(at.searles.fractbitmapmodel)

uint32_t width;
uint32_t height;
uint32_t pixelGap;

rs_allocation bitmap;

static float4 toRgbSqr(uchar4 u4) {
    float4 rgba = rsUnpackColor8888(u4);
    float4 sqrRgba = rgba * rgba;
    // preserve alpha
    sqrRgba.s3 = rgba.s3;
    return sqrRgba;
}

static uchar4 fromRgbSqr(float4 toRgbaSqr) {
    float4 rgba = native_sqrt(toRgbaSqr);
    rgba.s3 = toRgbaSqr.a;
    return rsPackColorTo8888(rgba);
}

uchar4 RS_KERNEL root(uint32_t x, uint32_t y) {
    uint32_t x0 = (x / pixelGap) * pixelGap;
    uint32_t x1 = x0 + pixelGap;
    uint32_t y0 = (y / pixelGap) * pixelGap;
    uint32_t y1 = y0 + pixelGap;

    float xRatio = (float) (x - x0) / (float) pixelGap;
    float yRatio = (float) (y - y0) / (float) pixelGap;

    if(x1 < width && y1 < height) {
        return fromRgbSqr(
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x0, y0)) * (1.f - xRatio) * (1.f - yRatio) +
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x1, y0)) * xRatio * (1.f - yRatio) +
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x0, y1)) * (1.f - xRatio) * yRatio +
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x1, y1)) * xRatio * yRatio);
    } else if(x1 < width) {
        return fromRgbSqr(
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x0, y0)) * (1.f - xRatio) +
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x1, y0)) * xRatio);
    } else if(y1 < height) {
        return fromRgbSqr(
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x0, y0)) * (1.f - yRatio) +
                toRgbSqr(rsGetElementAt_uchar4(bitmap, x0, y1)) * yRatio);
    } else {
        return rsGetElementAt_uchar4(bitmap, x0, y0);
    }
}