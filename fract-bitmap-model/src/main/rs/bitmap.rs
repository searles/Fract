/*
 * Kernel outputs a bitmap that it also takes as input.
 */

#pragma version(1)
#pragma rs java_package_name(at.searles.fractbitmapmodel)

#include "palette.rsh"
#include "colormodels.rsh"
#include "light.rsh"

uint32_t width;
uint32_t height;

int useLightEffect;

float3 *bitmapData;

static uchar4 to8888(float4 color) {
    // if color model is changedi it must be updated here and in PaletteUpdater.kt
    return rsPackColorTo8888(yuvToRgb(color));
}

uchar4 RS_KERNEL root(uint32_t x, uint32_t y) {

    float3 p00 = bitmapData[x + y * (width + 1)];
    float3 p10 = bitmapData[x + y * (width + 1) + 1];
    float3 p01 = bitmapData[x + (y + 1) * (width + 1)];
    float3 p11 = bitmapData[x + (y + 1) * (width + 1) + 1];

    // color is YUV, ie color.s0 is intensity.
    float4 color00 = colorAt(p00.x, p00.y);
    float4 color10 = colorAt(p10.x, p10.y);
    float4 color01 = colorAt(p01.x, p01.y);
    float4 color11 = colorAt(p11.x, p11.y);

    // correct brightness by using average of squares
    float avgSqrY = native_sqrt((color00.s0 * color00.s0 + color10.s0 * color10.s0 + color01.s0 * color01.s0 + color11.s0 * color11.s0) / 4.f);

    float4 color = (color00 + color10 + color01 + color11) / 4.0f;
    color.s0 = avgSqrY;

    if(useLightEffect == 0) {
        return to8888(color);
    }

    double scaleTo01Factor = ((double) min(width, height) / 2.0);

    float4 colorLight = adjustLight(color, (p10.z - p00.z) * scaleTo01Factor, (p01.z - p00.z) * scaleTo01Factor);

    return to8888(colorLight);
}

uint32_t pixelGap; // for the fast kernel.

uchar4 RS_KERNEL fastRoot(uint32_t x, uint32_t y) {
    if(x % pixelGap != 0 || y % pixelGap != 0) {
        return (uchar4) { 0, 0, 0, 0 };
    }

    float3 p00 = bitmapData[x + y * (width + 1)];
    float4 color = colorAt(p00.x, p00.y);

    if(useLightEffect == 0 || x + pixelGap > width || y + pixelGap > height) {
        return to8888(color);
    }

    float3 p10 = bitmapData[x + y * (width + 1) + pixelGap];
    float3 p01 = bitmapData[x + (y + pixelGap) * (width + 1)];

    double scaleTo01Factor = ((double) min(width, height) / 2.0) / (double) pixelGap;

    float4 finalColor = adjustLight(color, (p10.z - p00.z) * scaleTo01Factor, (p01.z - p00.z) * scaleTo01Factor);

    return to8888(finalColor);
}