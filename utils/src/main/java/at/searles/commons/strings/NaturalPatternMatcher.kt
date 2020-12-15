package at.searles.commons.strings

object NaturalPatternMatcher {
    /**
     * @return true if input contains all characters in pattern in the given order, ignoring case match.
     */
    fun match(input: CharSequence, pattern: String): Boolean = match(input, pattern) { _, _ -> }

    /**
     * Similar to match, but this method can be used to highlight matching fragments in input.
     * The main use of this method is in combination with Android's SpannableString.
     */
    fun match(input: CharSequence, pattern: String, callback: (Int, Int) -> Unit): Boolean {
        var index0 = 0
        var index1 = 0

        var spanStart = -1

        while(index0 < input.length && index1 < pattern.length) {
            val cp0 = Character.codePointAt(input, index0)
            val cp1 = pattern.codePointAt(index1)

            if(Character.toLowerCase(cp0) == Character.toLowerCase(cp1)) {
                if(spanStart < 0) spanStart = index0
                index1 += Character.charCount(cp1)
            } else if(spanStart >= 0) { // no match, end span.
                callback.invoke(spanStart, index0)
                spanStart = -1
            }

            index0 += Character.charCount(cp0)
        }

        if(spanStart >= 0) callback.invoke(spanStart, index0)
        return index1 >= pattern.length
    }
}