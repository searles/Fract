package at.searles.fractimageview

import android.graphics.Matrix
import android.graphics.PointF
import android.util.Pair
import java.util.*
import kotlin.math.abs

/**
 * This multitouch-controller allows to create a scale-event from
 * multiple gestures.
 */
class MultiTouchController(
    private val hasRotationLock: Boolean,
    private val hasCenterLock: Boolean
) {

    /**
     * Matrix contains index -> moved from, to-pairs since last change.
     */
    private val points = TreeMap<Int, Pair<PointF, PointF>>()

    /**
     * Matrix from last touch gestures
     */
    private val gestureMatrix: Matrix = Matrix()

    /**
     * Matrix from current set of touch gesture.
     */
    private val currentMatrix: Matrix
        get() = when(points.size) {
                0 -> Matrix()
                1 -> createMatrixFromGesture(points.values.first())
                2 -> createMatrixFromGesture(points.values.first(), points.values.elementAt(1))
                else -> createMatrixFromGesture(
                    points.values.first(),
                    points.values.elementAt(1),
                    points.values.elementAt(2))
            }

    /**
     * Combined gestureMatrix with subgesture.
     */
    val matrix: Matrix
        get() = currentMatrix.apply {
            preConcat(gestureMatrix)
        }

    private fun createMatrixFromGesture(pq0: Pair<PointF, PointF>, pq1: Pair<PointF, PointF>, pq2: Pair<PointF, PointF>): Matrix {
        // Get the matrix
        // ( a b tx )
        // ( c d ty )
        // ( 0 0  1 )
        // m * p0 = q0
        // m * p1 = q1
        // m * p2 = q2
        val p0x = pq0.first.x
        val p0y = pq0.first.y
        val p1x = pq1.first.x
        val p1y = pq1.first.y
        val p2x = pq2.first.x
        val p2y = pq2.first.y
        val q0x = pq0.second.x
        val q0y = pq0.second.y
        val q1x = pq1.second.x
        val q1y = pq1.second.y
        val q2x = pq2.second.x
        val q2y = pq2.second.y

        val det = p0x * p1y + p1x * p2y + p2x * p0y -
                (p0x * p2y + p1x * p0y + p2x * p1y)

        val detA = q0x * p1y + q1x * p2y + q2x * p0y -
                (q0x * p2y + q1x * p0y + q2x * p1y)

        val detB = p0x * q1x + p1x * q2x + p2x * q0x -
                (p0x * q2x + p1x * q0x + p2x * q1x)

        val detTx = p0x * p1y * q2x + p1x * p2y * q0x + p2x * p0y * q1x -
                (p0x * p2y * q1x + p1x * p0y * q2x + p2x * p1y * q0x)

        val detC = q0y * p1y + q1y * p2y + q2y * p0y -
                (q0y * p2y + q1y * p0y + q2y * p1y)

        val detD = p0x * q1y + p1x * q2y + p2x * q0y -
                (p0x * q2y + p1x * q0y + p2x * q1y)

        val detTy = p0x * p1y * q2y + p1x * p2y * q0y + p2x * p0y * q1y -
                (p0x * p2y * q1y + p1x * p0y * q2y + p2x * p1y * q0y)

        val a = detA / det
        val b = detB / det
        val tx = if(hasCenterLock) 0f else (detTx / det)
        val c = detC / det
        val d = detD / det
        val ty = if(hasCenterLock) 0f else (detTy / det)

        return Matrix().apply {
            if(hasRotationLock) {
                if(abs(a * d) > abs(b * c)) {
                    setValues(floatArrayOf(
                        a, 0f, tx,
                        0f, d, ty,
                        0f, 0f, 1f))
                } else {
                    setValues(floatArrayOf(
                        0f, b, tx,
                        c, 0f, ty,
                        0f, 0f, 1f))
                }
            } else {
                setValues(floatArrayOf(
                    a, b, tx,
                    c, d, ty,
                    0f, 0f, 1f))
            }
        }
    }

    private fun createMatrixFromGesture(pq0: Pair<PointF, PointF>, pq1: Pair<PointF, PointF>): Matrix {
        val p0x = pq0.first.x
        val p0y = pq0.first.y
        val p1x = pq1.first.x
        val p1y = pq1.first.y
        val q0x = pq0.second.x
        val q0y = pq0.second.y
        val q1x = pq1.second.x
        val q1y = pq1.second.y

        val dpx = p0x - p1x
        val dpy = p0y - p1y
        val dqx = q0x - q1x
        val dqy = q0y - q1y

        val det = dpx * -dpx - dpy * dpy
        val detR = dqx * -dpx - dqy * dpy
        val detS = dpx * dqy - dpy * dqx

        val r = detR / det
        val s = detS / det

        val tx = if(hasCenterLock) 0f else (q0x - r * p0x - s * p0y)
        val ty = if(hasCenterLock) 0f else (q0y - r * p0y + s * p0x)

        return Matrix().apply {
            if(hasRotationLock) {
                if(abs(s) > abs(r)) {
                    setValues(floatArrayOf(0f, s, tx, -s, 0f, ty, 0f, 0f, 1f))
                } else {
                    setValues(floatArrayOf(r, 0f, tx, 0f, r, ty, 0f, 0f, 1f))
                }
            } else {
                setValues(floatArrayOf(r, s, tx, -s, r, ty, 0f, 0f, 1f))
            }
        }
    }

    private fun createMatrixFromGesture(pq: Pair<PointF, PointF>): Matrix {
        val tx = if(hasCenterLock) 0f else (pq.second.x - pq.first.x)
        val ty = if(hasCenterLock) 0f else (pq.second.y - pq.first.y)

        return Matrix().apply {
            setValues(floatArrayOf(1f, 0f, tx, 0f, 1f, ty, 0f, 0f, 1f))
        }
    }

    /**
     * After every change of a touch event, commit must be called.
     */
    private fun commit() { // commit current changes.
        gestureMatrix.set(matrix)

        // reset points
        for (p in points.entries) {
            p.setValue(Pair(p.value.second, p.value.second))
        }
    }

    /**
     * Called on pointer down
     */
    fun pointDown(pointerId: Int, pos: PointF) {
        commit()
        points[pointerId] = Pair(pos, pos)
    }

    /**
     * Called on pointer move
     */
    fun pointDrag(pointerId: Int, pos: PointF) {
        require(points.containsKey(pointerId)) { "trying to update non-existent index" }
        val entry = points[pointerId]
        points[pointerId] = Pair(entry!!.first, pos)
    }

    /**
     * Called on pointer up
     */
    fun pointUp(pointerId: Int) {
        require(points.containsKey(pointerId)) { "trying to remove non-existent index" }
        commit() // commit overrides points
        require(points.remove(pointerId) != null) { "remove returned null" }
    }

}