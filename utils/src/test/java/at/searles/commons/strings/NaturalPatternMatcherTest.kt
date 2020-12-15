package at.searles.commons.strings

import org.junit.Assert
import org.junit.Test

class NaturalPatternMatcherTest {
    @Test
    fun testBasicLogic() {
        Assert.assertTrue(NaturalPatternMatcher.match("abc", ""))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "a"))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "b"))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "c"))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "ab"))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "ac"))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "bc"))
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "abc"))

        Assert.assertFalse(NaturalPatternMatcher.match("abc", "d"))
        Assert.assertFalse(NaturalPatternMatcher.match("abc", "ba"))
    }

    @Test
    fun testCaseLogic() {
        Assert.assertTrue(NaturalPatternMatcher.match("abc", "A"))
        Assert.assertTrue(NaturalPatternMatcher.match("Abc", "a"))
    }

    @Test
    fun testMatch() {
        NaturalPatternMatcher.match("abc", "a") { start, end -> Assert.assertTrue(start == 0 && end == 1)}
        NaturalPatternMatcher.match("abc", "b") { start, end -> Assert.assertTrue(start == 1 && end == 2)}
        NaturalPatternMatcher.match("abc", "c") { start, end -> Assert.assertTrue(start == 2 && end == 3)}
        NaturalPatternMatcher.match("abc", "ab") { start, end -> Assert.assertTrue(start == 0 && end == 2)}
        NaturalPatternMatcher.match("abc", "ac") { start, end -> Assert.assertTrue(end - start == 1 && (start == 0 || start == 2))}
    }
}