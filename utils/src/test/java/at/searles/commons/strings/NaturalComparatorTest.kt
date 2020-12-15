package at.searles.commons.strings

import org.junit.Assert
import org.junit.Test

internal class NaturalComparatorTest {
    @Test
    fun reflexiveTest() {
        Assert.assertEquals(0, NaturalComparator.compare("", ""))
        Assert.assertEquals(0, NaturalComparator.compare(".", "."))
        Assert.assertEquals(0, NaturalComparator.compare("..", ".."))
        Assert.assertEquals(0, NaturalComparator.compare("+", "+"))
        Assert.assertEquals(0, NaturalComparator.compare("ab", "ab"))
        Assert.assertEquals(0, NaturalComparator.compare("00", "00"))
        Assert.assertEquals(0, NaturalComparator.compare("01", "01"))
        Assert.assertEquals(0, NaturalComparator.compare(".0a", ".0a"))
        Assert.assertEquals(0, NaturalComparator.compare(String(intArrayOf(0x10fff), 0, 1), String(intArrayOf(0x10fff), 0, 1)))
    }

    private fun checkCmp(smaller: String, larger: String) {
        Assert.assertTrue(NaturalComparator.compare(smaller, larger) < 0)
        Assert.assertTrue(NaturalComparator.compare(larger, smaller) > 0)
    }

    @Test
    fun emptyVsStringsTest() {
        checkCmp("", "a")
        checkCmp("", "0")
        checkCmp("", ".")
    }

    @Test
    fun testAllPermutations() {
        checkCmp("a", "aa")
        checkCmp("a", "ab")
        checkCmp("ab", "ac")

        // check upper-case/lower-case
        checkCmp("a", "Aa")
        checkCmp("Aa", "aa")
        checkCmp("appLe", "apple")
        checkCmp("Apple", "aPple")
        checkCmp("a", "B")
        checkCmp("B", "C")
        checkCmp("C", "c")

        checkCmp(".", "..")
        checkCmp("+", "--")
        checkCmp("+", "-")

        checkCmp("0", "00")
        checkCmp("2", "10")
        checkCmp("09", "10")
        checkCmp("9", "010")
        checkCmp("10", "00010")

        checkCmp("a(1)", "a2")
        checkCmp("a(1)", "a((2))")
        checkCmp("a(1)", "a((2))")
        checkCmp("a(1)", "A((2))")
        checkCmp("a_1", "a++++++++++2")

        checkCmp("a.", "ab")
        checkCmp("a", "a0")
        checkCmp("a.", "a0")
        checkCmp("a0", "a00")
        checkCmp("a00", "a1")
        checkCmp("a1", "a1.")
        checkCmp("a1", "ab")
        checkCmp(".0.", ".00")
        checkCmp("a..c", "a.c")
        checkCmp("a.b", "a..c")

        checkCmp(String(intArrayOf(0x10fff), 0, 1), String(intArrayOf(0x11000), 0, 1))
        checkCmp(String(intArrayOf(0x10fff), 0, 1) + ".", String(intArrayOf(0x10fff), 0, 1) + "a")
    }
}