package at.searles.fractlang

import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.parsing.format.CodeFormatter
import at.searles.parsing.format.EditableString
import org.junit.Assert
import org.junit.Test

class FormatCodeTest {
    private lateinit var output: String

    @Test
    fun test() {
        format("{var a = 1;}")

        Assert.assertEquals("{\n" +
                "    var a = 1;\n" +
                "}", output)

    }

    @Test
    fun testStability() {
        format("{var a = 1;}")
        format(output)
        format(output)
        format(output)

        Assert.assertEquals("{\n" +
                "    var a = 1;\n" +
                "}", output)

    }

    private fun format(source: String) {
        val editable = EditableString(source)
        CodeFormatter(FractlangGrammar.program, FractlangGrammar.whiteSpaceId).format(editable)
        output = editable.toString()
    }
}