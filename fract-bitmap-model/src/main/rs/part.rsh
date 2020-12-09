/*
 * Header file with functions to convert indices of parts of the image
 * to bitmap coordinates.
 */

// dimensions of bitmap. range of data is from 0 to inclusive width!
uint32_t width;
uint32_t height;

uint32_t pixelIndex0;
int ceilLog2Width;
int ceilLog2Height;

static int2 getPixelCoordinates(int index) {
    int x = 0;
    int y = 0;

    for(int a = 0; a < ceilLog2Width - min(ceilLog2Width, ceilLog2Height); a++) {
        x = (x << 1) | ((index >> a) & 1);
    }

    for(int a = 0; a < ceilLog2Height - min(ceilLog2Width, ceilLog2Height); a++) {
        y = (y << 1) | ((index >> a) & 1);
    }

    for(int a = abs(ceilLog2Width - ceilLog2Height); a < ceilLog2Width + ceilLog2Height; a += 2) {
        y = (y << 1) | ((index >> (a + 1)) & 1);
        x = (x << 1) | ((index >> a) & 1);
    }

    return (int2) {x, y};
}


