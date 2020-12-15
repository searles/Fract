import at.searles.commons.geometry.Arc
import at.searles.commons.math.Cplx
import org.junit.Assert.assertEquals
import org.junit.Test

class ArcTest {
    @Test
    fun testArcLeftRight() {
        val arc = Arc(Cplx(0.0, 0.0), Cplx(2.0, 0.0), 1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcRightLeft() {
        val arc = Arc(Cplx(2.0, 0.0), Cplx(0.0, 0.0), 1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcLeftRightNegR() {
        val arc = Arc(Cplx(0.0, 0.0), Cplx(2.0, 0.0), -1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcRightLeftNegR() {
        val arc = Arc(Cplx(2.0, 0.0), Cplx(0.0, 0.0), -1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcLeftUp() {
        val arc = Arc(Cplx(0.0, 0.0), Cplx(1.0, 1.0), 1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)

        assertEquals(0.0, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcUpRight() {
        val arc = Arc(Cplx(1.0, 1.0), Cplx(2.0, 0.0), 1.0)

        assertEquals(1.4142135623730951, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcRightDown() {
        val arc = Arc(Cplx(2.0, 0.0), Cplx(1.0, -1.0), 1.0)

        assertEquals(1.4142135623730951, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.0, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcDownLeft() {
        val arc = Arc(Cplx(1.0, -1.0), Cplx(0.0, 0.0), 1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(2.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(1.4142135623730951, arc.dist(1.0, 1.0), 1e-9)
        assertEquals(0.0, arc.dist(1.0, -1.0), 1e-9)
    }

    @Test
    fun testArcUpLeft() {
        val arc = Arc(Cplx(1.0, 1.0), Cplx(0.0, 0.0), 1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(0.41421356237309515, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 2.0), 1e-9)
    }

    @Test
    fun testArcUpLeftNegR() {
        val arc = Arc(Cplx(1.0, 1.0), Cplx(0.0, 0.0), -1.0)

        assertEquals(0.0, arc.dist(0.0, 0.0), 1e-9)
        assertEquals(1.0, arc.dist(1.0, 0.0), 1e-9)
        assertEquals(0.41421356237309515, arc.dist(1.0, 2.0), 1e-9)
    }
}