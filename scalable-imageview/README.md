# ScaleableImageView

Provides classes for Android to modify a bitmap using matrix-based scale events.

## Some maths

### Touch events to Matrix

The translation of finger touch events to matrixes is complex but straightforward in
MultiTouchController.kt. Depending on the number of pointers, the following behaviour is
implemented.

* Double tap: Zoom in
* 1: Simply move
* 2: Scale keeping the ratio, rotate and move
* 3: Shearing

### View coordinates, image coordinates, matrices

There are multiple elements influencing the way the bitmap is drawn:

* The view coordinates (in the following $vw$ and $vh$)
* The bitmap coordinates ($bw$ and $bh$)
* The current (normalized) scale event $m$
* The current bitmap matrix (normally the last scale scaled up to bitmap coordinates).

The bitmap is centered scaled inside the view and eventually flipped.

### Translate from view coordinates to normalized coordinates

Consider that the image might be flipped.

~~~
    N' = (2 P - (vw, vh)) / min(vw, vh)
    N = if(flipped) N'^T else N'
~~~

Example:

~~~
V = (200, 100)

P_1 = (0, 0)
P_2 = (100, 50)
P_3 = (150, 100)

N_1' = (-2, -1)
N_2' = (0, 0)
N_3' = (1, 1)
~~~

### Current matrix for image view

#### Step 1: Matrix from view to bitmap.

* bitmapToNorm is the matrix that converts from bitmap coordinates to normalized.
* normToBitmap is the matrix that converts from normalized coordinates to bitmap.
* controllerMatrix is the current scale matrix.
* normMatrix is the matrix of the latest scale event
* bitmapToView is the matrix that converts from bitmap coordinates to view coordinates.

~~~
    bitmapToView * normToBitmap* controllerMatrix * normMatrix * bitmapToNorm
~~~

Example:

~~~
controller and norm are the identity, then the result is bitmapToView.

norm = [[2, 0, 0], [0, 2, 0], [0, 0, 1]]
controller = identity
~~~


~~~
    newImageMatrix =
    viewMatrix = controller!!.matrix
    viewMatrix.preConcat(bitmapProvider.normMatrix)

    newImageMatrix.preConcat(
        bitmap2norm(
            bitmap!!.width,
            bitmap!!.height
        )
    )
    newImageMatrix.postConcat(
        norm2bitmap(
            bitmap!!.width,
            bitmap!!.height
        )
    )
    newImageMatrix.postConcat(bitmapToView)
~~~