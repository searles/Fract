package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import org.junit.Assert
import org.junit.Test

class RectTest {
    @Test
    fun sanityTest() {
        val rect = Rect(Cplx(0.0), Cplx(1.0, 1.0))

        Assert.assertEquals(1.0, rect.dist(-1.0, 0.0), 1e-9)
        Assert.assertEquals(0.5, rect.dist(0.5, 0.5), 1e-9)
        Assert.assertEquals(1.0, rect.dist(2.0, 0.0), 1e-9)
        Assert.assertEquals(1.0, rect.dist(0.0, 2.0), 1e-9)
        Assert.assertEquals(1.4142135623730951, rect.dist(-1.0, -1.0), 1e-9)
    }
}