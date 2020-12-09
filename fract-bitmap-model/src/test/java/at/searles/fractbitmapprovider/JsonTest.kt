package at.searles.fractbitmapprovider

import at.searles.fractlang.FractlangProgram
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test

class JsonTest {
    @Test
    fun testVerySimply() {
        withSourceCode("extern a:\"A\" = \"1\";")
        createJson()
        Assert.assertEquals("", output)
    }

    private lateinit var calcProperties: CalcProperties
    private lateinit var output: String

    private fun withSourceCode(sourceCode: String) {
        calcProperties = CalcProperties(null, FractlangProgram(sourceCode, emptyMap()))
    }

    private fun createJson() {
        val obj = JSONObject()
        calcProperties.createJson(obj)
        output = obj.toString(4)
    }
}