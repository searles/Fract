#pragma version(1)
#pragma rs java_package_name(at.searles.fractbitmapmodel)
#pragma rs_fp_relaxed

/*
 * This file performs all calculations. For this purpose, it
 * always calculates a tile of a given size which is then
 * copied into the real data. Another script translates this
 * information into bitmap colors.
 */

/*
 * Float3 has the following structure:
 * xValue, ranging from 0-1, higher value is the palette index.
 * yValue, ranging from 0-1.
 * height.
 * To indicate that a point is outdated, it should be set to
 */
#include "scale.rsh"
#include "part.rsh"
#include "vm.rsh"
#include "mandelbrot.rsh"

// ---------------------------------------------------------------------------------------------- //

float3 RS_KERNEL calculate(float3 in, uint32_t x) {
    uint32_t y = x / (width + 1);
    x = x % (width + 1);

    double2 pt = mapCoordinates(x, y);
    float3 value = valueAt(pt);

    return value;
}

int count; // number of pixels to be drawn in total.

float3 __attribute__((kernel)) calculate_part(uint32_t x) { // name x is mandatory
    uint32_t pixelIndex = pixelIndex0 + x;

    if(pixelIndex >= count) {
        return 0;
    }

    int2 px = getPixelCoordinates(pixelIndex);

    if(px.x > width || px.y > height) {
        return 0;
    }

    double2 pt = mapCoordinates(px.x, px.y);
    float3 value = valueAt(pt);

    return value;
}

rs_allocation calcData;

float3 __attribute__((kernel)) copy_part(float3 inValue, uint32_t x) { // name x is mandatory
    uint32_t pixelIndex = pixelIndex0 + x;

    if(pixelIndex >= count) {
        return 0;
    }

    int2 px = getPixelCoordinates(pixelIndex);

    if(px.x <= width && px.y <= height) {
        rsSetElementAt_float3(calcData, inValue, px.y * (width + 1) + px.x);
    }

    return 0;
}