package at.searles.commons.utils

import at.searles.commons.util.IntIntMap
import org.junit.Assert
import org.junit.Test

class IntIntMapTest {
    @Test
    fun simpleTest() {
        val map = IntIntMap<String>()

        Assert.assertTrue(map.isEmpty())
        Assert.assertEquals(0, map.size())

        Assert.assertNull(map[0, 0])

        map[0, 0] = "A"

        Assert.assertEquals("A", map[0, 0])

        map[10, 1] = "B"
        map[1, 10] = "C"

        Assert.assertEquals("B", map[10, 1])
        Assert.assertEquals("C", map[1, 10])

        Assert.assertEquals(3, map.size())
    }
}