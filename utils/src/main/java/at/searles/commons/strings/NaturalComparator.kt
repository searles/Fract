package at.searles.commons.strings

object NaturalComparator: Comparator<String> {
    /**
     * Imposes an order on the following rules:
     * Symbols like $+/ are skipped unless the strings are equal otherwise.
     * Numbers are grouped. If two numbers are equal, the longer one (due to leading 0) is larger
     * For latin letters, upper case and lower case is ignored unless there is no other criterion left. In that case,
     * upper case is considered smaller (sounds weird, but I guess this is common practice).
     * Otherwise, the unicode-order is used.
     */
    override fun compare(s0: String, s1: String): Int {
        var index0 = 0
        var index1 = 0

        var asciiSymbolCmp = 0
        var upperCaseLowerCaseCmp = 0 // comparator of upper-case/lower-case. Used if everything else fails.

        // here comes a long loop...
        while(index0 < s0.length && index1 < s1.length) {
            // none of them is empty.

            // ascii symbols are ignored unless the strings are apart from that equal.
            if(isAsciiSymbol(s0, index0)) {
                if(isAsciiSymbol(s1, index1)) {
                    if(asciiSymbolCmp == 0) asciiSymbolCmp = s0[index0].compareTo(s1[index1])
                    index0 ++
                    index1 ++
                    continue // back to loop
                }

                if(asciiSymbolCmp == 0) asciiSymbolCmp = -1
                index0++
                continue
            }

            if(isAsciiSymbol(s1, index1)) {
                if(asciiSymbolCmp == 0) asciiSymbolCmp = 1
                index1++
                continue
            }

            // two numbers?
            val n0 = numberChunkLength(s0, index0)
            val n1 = numberChunkLength(s1, index1)

            if((n0 == 0) xor (n1 == 0)) {
                return n1 - n0
            } else if(n0 > 0 && n1 > 0) {
                val cmp = cmpNumberChunk(s0, s1, index0, index1, n0, n1)
                if(cmp != 0) return cmp
                index0 += n0
                index1 += n1
                continue // back to loop.
            }

            // check alphanumeric, agnostic of upper-case/lower-case
            if(isAlpha(s0, index0) && isAlpha(s1, index1)) {
                val cmp = Character.toLowerCase(s0[index0]).compareTo(Character.toLowerCase(s1[index1]))
                if(cmp != 0) return cmp
                if(upperCaseLowerCaseCmp == 0) {
                    // but store if there is a difference for later.
                    upperCaseLowerCaseCmp = s0[index0].compareTo(s1[index1])
                }
                index0 ++
                index1 ++
                continue // back to loop
            }

            // finally, compare single codepoints
            val cp0 = s0.codePointAt(index0)
            val cp1 = s1.codePointAt(index1)

            val cmp = cp0.compareTo(cp1)
            if(cmp != 0) return cmp
            index0 += Character.charCount(cp0)
            index1 += Character.charCount(cp1)
        }

        val cmp = (s0.length - index0) - (s1.length - index1)

        if(cmp != 0) return cmp

        if(asciiSymbolCmp != 0) return asciiSymbolCmp
        return upperCaseLowerCaseCmp
    }

    private fun isNumber(s: String, index: Int): Boolean {
        return index < s.length && (s[index] in '0'..'9')
    }

    private fun isAsciiSymbol(s: String, index: Int): Boolean {
        return index < s.length && s[index] < 0xff.toChar() && !isNumber(s, index) && !isAlpha(s, index)
    }

    private fun isAlpha(s: String, index: Int): Boolean {
        return index < s.length && (s[index] in 'A'..'Z' || s[index] in 'a'..'z')
    }

    private fun numberChunkLength(s: String, index: Int): Int {
        var len = 0

        // it is a sequence of digits
        while(isNumber(s, index + len)) {
            len++
        }

        return len
    }

    private fun cmpNumberChunk(s0: String, s1: String, index0: Int, index1: Int, len0: Int, len1: Int): Int {
        // check from behind
        var i0 = len0 - 1
        var i1 = len1 - 1

        var cmp = 0

        while(i0 >= 0 || i1 >= 0) {
            val digit0 = if (i0 >= 0) s0[index0 + i0] else '0'
            val digit1 = if (i1 >= 0) s1[index1 + i1] else '0'

            val cmpDigit = digit0.compareTo(digit1)

            if(cmpDigit != 0) {
                cmp = cmpDigit
            }

            i0--
            i1--
        }

        if(cmp != 0) {
            return cmp
        }

        // 4 comes before 04.
        return len0 - len1
    }
}