package at.searles.sourceeditor

import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DelayedUpdaterTest {
    private val delay: Long = 20L

    private val list = ArrayList<Int>()
    private val updater = DelayedUpdater(
            Runnable { list.add(1) }, delay)

    @Before
    fun setUp() {
        list.clear()
    }

    @Test
    fun testCleanState() {
        Assert.assertFalse(updater.isScheduled())
        Assert.assertTrue(list.isEmpty())
    }

    @Test
    fun test1Tick() {
        updater.tick()

        Thread.sleep(delay * 2 / 3)

        Assert.assertTrue(list.isEmpty())
        Assert.assertTrue(updater.isScheduled())

        Thread.sleep(delay * 2 / 3)

        Assert.assertEquals(1, list.size)
        Assert.assertFalse(updater.isScheduled())
    }

    @Test
    fun test2TickInterleaved() {
        updater.tick()

        Thread.sleep(delay * 2 / 3)

        Assert.assertTrue(list.isEmpty())
        Assert.assertTrue(updater.isScheduled())

        updater.tick()

        Thread.sleep(delay * 2 / 3)

        Assert.assertTrue(list.isEmpty())
        Assert.assertTrue(updater.isScheduled())

        Thread.sleep(delay * 2 / 3)

        Assert.assertEquals(1, list.size)
        Assert.assertFalse(updater.isScheduled())
    }


    @Test
    fun test2TickNonInterleaved() {
        updater.tick()

        Thread.sleep(delay * 4 / 3)

        Assert.assertEquals(1, list.size)
        Assert.assertFalse(updater.isScheduled())

        updater.tick()

        Thread.sleep(delay * 2 / 3)

        Assert.assertEquals(1, list.size)
        Assert.assertTrue(updater.isScheduled())

        Thread.sleep(delay * 2 / 3)

        Assert.assertEquals(2, list.size)
        Assert.assertFalse(updater.isScheduled())
    }
}