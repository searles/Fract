package at.searles.commons.geometry

import at.searles.commons.math.Cplx
import org.junit.Assert
import org.junit.Test

class CircleTest {
    @Test
    fun testSanity() {
        val circle = Circle(Cplx(0.0), 1.0)

        Assert.assertEquals(1.0, circle.dist(0.0, 0.0), 1e-9)
        Assert.assertEquals(0.0, circle.dist(-1.0, 0.0), 1e-9)
        Assert.assertEquals(1.0, circle.dist(0.0, 2.0), 1e-9)
    }
}