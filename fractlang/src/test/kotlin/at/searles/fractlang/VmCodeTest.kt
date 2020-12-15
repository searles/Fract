package at.searles.fractlang

import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.parsing.ParserStream
import org.junit.Assert
import org.junit.Test
import java.io.File

class VmCodeTest {
    @Test
    fun testSimpleAssignment() {
        withSource("var b = 99;")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 99), vmCode.toList())
    }

    @Test
    fun testMandelbrot() {
        withSource("val z0 = 0:0;\n" +
                "var c = point;\n" +
                "var n = 0;\n" +
                "\n" +
                "var z = z0;\n" +
                "\n" +
                "val bailoutValue = 64;\n" +
                "val maxExponent = 2;\n" +
                "val maxIterationCount = 1024;\n" +
                "\n" +
                "while ({\n" +
                "\tz = z^maxExponent + c;\n" +
                "\t\n" +
                "\tvar absZ = abs z;\n" +
                "\t\n" +
                "\tif(absZ > bailoutValue) {\n" +
                "\t\tvar continuousAddend = -log(absZ / log bailoutValue) / log maxExponent;\n" +
                "\t\tvar continuousN = n + continuousAddend;\n" +
                "\t\tsetResult(1, log (1 + continuousN), continuousN);\n" +
                "\t\tfalse\n" +
                "\t} else if(not next(maxIterationCount, n)) {\n" +
                "\t\tsetResult(0, arg z / 2 pi, log absZ);\n" +
                "\t\tfalse\n" +
                "\t} else {\n" +
                "\t\ttrue\n" +
                "\t}\n" +
                "})")

        actCreateVmCode()

        Assert.assertEquals(listOf(92, 0, 49, 4, 0, 53, 5, 0, 0, 0, 0, 33, 5, 2, 9, 4, 9, 0, 5, 47, 5, 13, 61, 0, 1078984704, 13, 28, 70, 15, -600177666, 1070515977, 13, 15, 70, 15, 15, 15, 1697350398, 1073157447, 15, 15, 41, 15, 15, 80, 4, 17, 2, 17, 15, 17, 3, 0, 1072693248, 17, 19, 70, 19, 19, 83, 19, 0, 0, 19, 94, 1, 19, 17, 54, 99, 64, 1024, 4, 97, 75, 84, 5, 15, 15, 1841940611, 1069834032, 15, 15, 83, 15, 0, 0, 9, 70, 13, 13, 94, 0, 9, 13, 54, 99, 54, 11), vmCode.toList())
    }

    @Test
    fun testIfBoolBlock() {
        withSource("var a = 1; if(if(a == 1) { a = a + 1; false } else { true }) a = a + 2;")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 1, 56, 1, 0, 8, 14, 1, 1, 0, 0, 54, 18, 1, 2, 0, 0), vmCode.toList())
    }

    @Test
    fun testMandelbrotBug0() {
        withSource("var n = 0;\n" +
                "var c = point;\n" +
                "c = 2;\n" +
                "var z = 0:0;\n" +
                "\n" +
                "while ({\n" +
                "\tz = z^2 + c;\n" +
                "\tvar absZ = abs z;\n" +
                "\tif(absZ > 4) {\n" +
                "\t\tsetResult(0, n, 0);\n" +
                "\t\tfalse\n" +
                "\t} else if(not next(10, n)) {\n" +
                "\t\tsetResult(1, 0, 0);\n" +
                "\t\tfalse\n" +
                "\t} else {\n" +
                "\t\ttrue\n" +
                "\t}\n" +
                "})")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 0, 92, 1, 53, 1, 0, 1073741824, 0, 0, 53, 5, 0, 0, 0, 0, 33, 5, 2, 9, 4, 9, 1, 5, 47, 5, 9, 61, 0, 1074790400, 9, 34, 49, 80, 0, 9, 83, 9, 0, 0, 9, 98, 0, 9, 0, 0, 54, 66, 64, 10, 0, 64, 54, 100, 1, 0, 0, 0, 0, 0, 0, 54, 66, 54, 17), vmCode.toList())
    }


    @Test
    fun testMandelbrotBug1() {
        withSource("var n = 0;\n" +
                "var c = point;\n" +
                "var z = 0:0;\n" +
                "\n" +
                "while ({\n" +
                "\tz = z*z + c;\n" + // here is a problem, probably an overlap of variables.
                "\tvar absZ = abs z;\n" +
                "\tif(absZ > 4) {\n" +
                "\t\tsetResult(0, n * 0.1, 0);\n" +
                "\t\tfalse\n" +
                "\t} else if(not next(10, n)) {\n" +
                "\t\tsetResult(1, 0, 0);\n" +
                "\t\tfalse\n" +
                "\t} else {\n" +
                "\t\ttrue\n" +
                "\t}\n" +
                "})")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 0, 92, 1, 53, 5, 0, 0, 0, 0, 16, 5, 5, 9, 4, 9, 1, 5, 47, 5, 9, 61, 0, 1074790400, 9, 28, 48, 80, 0, 9, 15, -1717986918, 1069128089, 9, 9, 83, 9, 0, 0, 9, 98, 0, 9, 0, 0, 54, 65, 64, 10, 0, 63, 53, 100, 1, 0, 0, 0, 0, 0, 0, 54, 65, 54, 11), vmCode.toList())
    }

    @Test
    fun testMandelbrotBug2() {
        withSource("var n = 0;\n" +
                "var c = point;\n" +
                "var z = 0:0;\n" +
                "\n" +
                "while ({\n" +
                "\tz = z^2 + c;\n" +
                "\n" +
                "\tvar absZ = abs z;\n" +
                "\n" +
                "\tif(absZ > 4) {\n" +
                "\t\tvar continuousAddend = 1.0 - absZ;\n" +
                "\n" +
                "\t\tif(continuousAddend < 0) continuousAddend = 0;\n" +
                "\t\tif(continuousAddend > 1) continuousAddend = 1;\n" +
                "\n" +
                "\t\tsetResult(0, n / 9.9, 0);\n" +
                "\t\tfalse\n" +
                "\t} else if(not next(10, n)) {\n" +
                "\t\tsetResult(1, 0, 0);\n" +
                "\t\tfalse\n" +
                "\t} else {\n" +
                "\t\ttrue\n" +
                "\t}\n" +
                "})")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 0, 92, 1, 53, 5, 0, 0, 0, 0, 33, 5, 2, 9, 4, 9, 1, 5, 47, 5, 9, 61, 0, 1074790400, 9, 28, 73, 9, 0, 1072693248, 9, 9, 62, 9, 0, 0, 39, 43, 51, 9, 0, 0, 61, 0, 1072693248, 9, 49, 53, 51, 9, 0, 1072693248, 80, 0, 11, 15, 1214738225, 1069145036, 11, 11, 83, 11, 0, 0, 11, 98, 0, 11, 0, 0, 54, 90, 64, 10, 0, 88, 78, 100, 1, 0, 0, 0, 0, 0, 0, 54, 90, 54, 11), vmCode.toList())
    }

    @Test
    fun testMandelbrotBug3() {
        withSource(
            "var c = point;\n" +
                "c = re c;\n")

        actCreateVmCode()

        Assert.assertEquals(listOf(92, 0, 85, 0, 4, 83, 4, 0, 0, 0), vmCode.toList())
    }


    @Test
    fun testSimpleMultiplicationError() {
        withSource("var a = 5 point;")

        actCreateVmCode()

        Assert.assertEquals(listOf(92, 0, 17, 0, 1075052544, 0, 0, 0, 0), vmCode.toList())
    }

    @Test
    fun testAddition() {
        withSource("var a = 50; var b = 25; var c = a + b;")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 50, 49, 1, 25, 0, 0, 1, 2), vmCode.toList())
    }

    @Test
    fun testAdditionWithConst() {
        withSource("var a = 50; a = a + 25;")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 50, 1, 25, 0, 0), vmCode.toList())
    }

    @Test
    fun testRealAddition() {
        withSource("var a = 50.0; var b = 25.0; var c = a + b;")

        actCreateVmCode()

        Assert.assertEquals(listOf(51, 0, 0, 1078525952, 51, 2, 0, 1077477376, 2, 0, 2, 4), vmCode.toList())
    }

    @Test
    fun testIfElseAndSelfAssignmentRemoval() {
        withSource("var a = 1; var b = 2; var c = if(a>b) a else b;")

        actCreateVmCode()

        Assert.assertEquals(listOf(49, 0, 1, 49, 1, 2, 57, 1, 0, 11, 16, 48, 2, 0, 54, 19, 48, 2, 1), vmCode.toList())
    }

    @Test
    fun testPointAndSetResult() {
        withSource("setResult(0, 0.5, 0);")

        actCreateVmCode()

        Assert.assertEquals(listOf(100, 0, 0, 1071644672, 0, 0, 0, 0), vmCode.toList())
    }

    @Test
    fun testCons() {
        withSource("setResult(0, sin abs point, 0);")

        actCreateVmCode()

        Assert.assertEquals(listOf(92, 0, 47, 0, 0, 72, 0, 0, 83, 0, 0, 0, 0, 98, 0, 0, 0, 0), vmCode.toList())
    }

    @Test
    fun testMandelbrotFile() {
        withSource(File("src/test/resources/mandelbrot_with_classes.ft").readText())
        actCreateVmCode()
        Assert.assertNotNull(source)
    }

    @Test
    fun testMandelbrot2File() {
        withSource(File("src/test/resources/mandelbrot2.ft").readText())
        actCreateVmCode()
        Assert.assertNotNull(source)
    }

    @Test
    fun testLyapunovFile() {
        withSource(File("src/test/resources/lyapunov.ft").readText())
        actCreateVmCode()
        Assert.assertNotNull(source)
    }

    private lateinit var ci: FractlangProgram
    private lateinit var source: String
    private lateinit var vmCode: IntArray

    private fun actCreateVmCode() {
        ci = FractlangProgram(source, emptyMap())
        vmCode = ci.vmCode
    }

    private fun withSource(src: String) {
        source = src
    }
}