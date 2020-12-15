package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import org.junit.Assert
import org.junit.Test

class LineTest {
    @Test
    fun sanityTest() {
        val line = Line(Cplx(0.0, 0.0), Cplx(1.0, 0.0))

        Assert.assertEquals(0.0, line.dist(Cplx(0.5, 0.0)), 1e-9)
        Assert.assertEquals(1.0, line.dist(Cplx(-1.0, 0.0)), 1e-9)
        Assert.assertEquals(1.0, line.dist(Cplx(2.0, 0.0)), 1e-9)
        Assert.assertEquals(1.0, line.dist(Cplx(0.5, -1.0)), 1e-9)
    }
}