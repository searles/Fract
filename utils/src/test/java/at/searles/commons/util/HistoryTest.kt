package at.searles.commons.util

import org.junit.Assert
import org.junit.Test

class HistoryTest {
    @Test
    fun simpleTest() {
        val history = History<Int>()

        history.add(1)

        Assert.assertFalse(history.hasBack())
        Assert.assertFalse(history.hasForward())

        history.add(2)

        Assert.assertTrue(history.hasBack())

        history.add(3)

        repeat(2) {
            Assert.assertTrue(history.hasBack())
            Assert.assertFalse(history.hasForward())

            Assert.assertEquals(2, history.back())

            Assert.assertTrue(history.hasForward())

            Assert.assertEquals(1, history.back())

            Assert.assertFalse(history.hasBack())

            Assert.assertEquals(2, history.forward())
            Assert.assertEquals(3, history.forward())
        }
    }
}