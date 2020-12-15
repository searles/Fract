package at.searles.fractlang

import at.searles.fractlang.linear.CodeLine
import at.searles.fractlang.linear.LinearizeStmt
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.ParserStream
import org.junit.Assert
import org.junit.Test

class LinearizationTest {

    @Test
    fun testRealConstantInBlockWithAddition() {
        withSource("var a = 0.1 + { var b = 1; 0.26};")
        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [b, 1]\n" +
                "Allocate b: Int\n" +
                "VarBound [b]\n" +
                "Assign[3] [_, 0.26]\n" +
                "Allocate _: Real\n" +
                "Add[3] [0.1, _, a]\n" +
                "Allocate a: Real\n" +
                "VarBound [a]", output)
    }

    @Test
    fun testRealConstantInBlockToComplex() {
        withSource("var a: Cplx = { var b = 1; 0.26};")
        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [b, 1]\n" +
                "Allocate b: Int\n" +
                "VarBound [b]\n" +
                "Assign[3] [_, 0.26]\n" +
                "Allocate _: Real\n" +
                "Cons[2] [_, 0.0, a]\n" +
                "Allocate a: Cplx\n" +
                "VarBound [a]", output)
    }

    @Test
    fun testSwitchExpr() {
        withSource("var a = 0; var b = [a + 1, a + 2, a + 3][a]")

        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [a, 0]\n" +
                "Allocate a: Int\n" +
                "Switch[0] [a, 3, Label case, Label case\$1, Label case\$2]\n" +
                "Label case\n" +
                "Add[1] [1, a, b]\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label case\$1\n" +
                "Add[1] [2, a, b]\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label case\$2\n" +
                "Add[1] [3, a, b]\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label endSwitch\n" +
                "Allocate b: Int\n" +
                "VarBound [a, b]", output)
    }

    @Test
    fun testSwitchBool() {
        withSource("var a = 0; var c: Int; if([a == 0, a == 1][a]) { c = 2 } else { c = 3 }; c = c + 4")

        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [a, 0]\n" +
                "Allocate a: Int\n" +
                "Allocate c: Int\n" +
                "Switch[0] [a, 2, Label case, Label case\$1]\n" +
                "Label case\n" +
                "Equal[1] [0, a, Label ifElseTrue, Label ifElseFalse]\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label case\$1\n" +
                "Equal[1] [1, a, Label ifElseTrue, Label ifElseFalse]\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label endSwitch\n" +
                "Label ifElseTrue\n" +
                "Assign[1] [c, 2]\n" +
                "VarBound []\n" +
                "Jump[0] [Label endIfElse]\n" +
                "Label ifElseFalse\n" +
                "Assign[1] [c, 3]\n" +
                "VarBound []\n" +
                "Label endIfElse\n" +
                "Add[1] [4, c, c]\n" +
                "VarBound [a, c]", output)
    }


    @Test
    fun testSwitchStmt() {
        withSource("var a = 0; var b: Int; [{b = 1}, {b = 2}][a]")

        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [a, 0]\n" +
                "Allocate a: Int\n" +
                "Allocate b: Int\n" +
                "Switch[0] [a, 2, Label case, Label case\$1]\n" +
                "Label case\n" +
                "Assign[1] [b, 1]\n" +
                "VarBound []\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label case\$1\n" +
                "Assign[1] [b, 2]\n" +
                "VarBound []\n" +
                "Jump[0] [Label endSwitch]\n" +
                "Label endSwitch\n" +
                "VarBound [a, b]", output)
    }

    @Test
    fun testConsts() {
        withSource("var a = pi + e + i;")

        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[5] [a, 5.859874482048838:1.0]\n" +
                "Allocate a: Cplx\n" +
                "VarBound [a]", output)
    }

    @Test
    fun avoidSameAssignment() {
        withSource("var b = 1; b = b")

        actParse()

        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [b, 1]\n" +
                "Allocate b: Int\n" +
                "VarBound [b]", output)
    }

    @Test
    fun testAssignWhileToVarFail() {
        withSource("var b = 1; var a = { while(b==2) {} }")

        actParse()

        try {
            actInline()
            actLinearize()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testSimpleVar() {
        withSource("var a = 1")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals("Assign[1] [a, 1]\n" +
                "Allocate a: Int\n" +
                "VarBound [a]", output)
    }

    @Test
    fun testAddition() {
        withSource("var a = 1;var b=2; var c=a + b + 3")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Assign[1] [b, 2]\n" +
                    "Allocate b: Int\n" +
                    "Add[1] [3, a, _]\n" +
                    "Allocate _: Int\n" +
                    "Add[0] [_, b, c]\n" +
                    "Allocate c: Int\n" +
                    "VarBound [a, b, c]", output)
    }

    @Test
    fun testBlock() {
        withSource("var a = 1 + {var b = 2; b + 3 }")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [b, 2]\n" +
                    "Allocate b: Int\n" +
                    "VarBound [b]\n" +
                    "Add[1] [3, b, _]\n" +
                    "Allocate _: Int\n" +
                    "Add[1] [1, _, a]\n" +
                    "Allocate a: Int\n" +
                    "VarBound [a]", output)
    }

    @Test
    fun testIfStmt() {
        withSource("var a = 1; if(a == 1) a = a + 1;")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Equal[1] [1, a, Label ifTrue, Label ifFalse]\n" +
                    "Label ifTrue\n" +
                    "Add[1] [1, a, a]\n" +
                    "Label ifFalse\n" +
                    "VarBound [a]", output)
    }

    @Test
    fun testSetResult() {
        withSource("setResult(0, 1 / point, re(point));")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Point[0] [_]\n" +
                    "Allocate _: Cplx\n" +
                    "Recip[1] [_, _\$1]\n" +
                    "Allocate _\$1: Cplx\n" +
                    "Point[0] [_\$2]\n" +
                    "Allocate _\$2: Cplx\n" +
                    "RealPart[0] [_\$2, _\$3]\n" +
                    "Allocate _\$3: Real\n" +
                    "SetResult[1] [0, _\$1, _\$3]\n" +
                    "VarBound []", output)
    }

    @Test
    fun testExpBug() {
        withSource("var z = 0:0; var a: Cplx = exp z;")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[5] [z, 0.0]\n" +
                    "Allocate z: Cplx\n" +
                    "Exp[1] [z, a]\n" +
                    "Allocate a: Cplx\n" +
                    "VarBound [z, a]", output)
    }

    @Test
    fun testIfElseStmt() {
        withSource("var a = 1; if(a == 1) a = a + 1 else a = a + 2")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Equal[1] [1, a, Label ifElseTrue, Label ifElseFalse]\n" +
                    "Label ifElseTrue\n" +
                    "Add[1] [1, a, a]\n" +
                    "Jump[0] [Label endIfElse]\n" +
                    "Label ifElseFalse\n" +
                    "Add[1] [2, a, a]\n" +
                    "Label endIfElse\n" +
                    "VarBound [a]", output)
    }

    @Test
    fun testIfElseExpr() {
        withSource("var a = 1; a = if(a == 1) 1 else 2")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Equal[1] [1, a, Label ifElseTrue, Label ifElseFalse]\n" +
                    "Label ifElseTrue\n" +
                    "Assign[1] [a, 1]\n" +
                    "Jump[0] [Label ifElseEnd]\n" +
                    "Label ifElseFalse\n" +
                    "Assign[1] [a, 2]\n" +
                    "Label ifElseEnd\n" +
                    "VarBound [a]", output)
    }

    @Test
    fun testIfElseLessExpr() {
        withSource("var a = 1; a = if(a < 1) 1 else 2")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Less[2] [a, 1, Label ifElseTrue, Label ifElseFalse]\n" +
                    "Label ifElseTrue\n" +
                    "Assign[1] [a, 1]\n" +
                    "Jump[0] [Label ifElseEnd]\n" +
                    "Label ifElseFalse\n" +
                    "Assign[1] [a, 2]\n" +
                    "Label ifElseEnd\n" +
                    "VarBound [a]", output)
    }

    @Test
    fun testModExpr() {
        withSource("var a = 1; var b = a % 2; var c = 2 % a;")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Mod[2] [a, 2, b]\n" +
                    "Allocate b: Int\n" +
                    "Mod[1] [2, a, c]\n" +
                    "Allocate c: Int\n" +
                    "VarBound [a, b, c]", output)
    }

    @Test
    fun testAssignRealToCplx() {
        withSource(
            "var c = point;\n" +
                    "c = re c;\n")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Point[0] [c]\n" +
                    "Allocate c: Cplx\n" +
                    "RealPart[0] [c, _]\n" +
                    "Allocate _: Real\n" +
                    "Cons[2] [_, 0.0, c]\n" +
                    "VarBound [c]", output)
    }

    @Test
    fun testInlineMulWithId() {
        withSource("var c = point; c = c c;\n")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Point[0] [c]\n" +
                    "Allocate c: Cplx\n" +
                    "Mul[4] [c, c, c]\n" +
                    "VarBound [c]", output)
    }

    @Test
    fun testVectorWithDifferentTypes() {
        withSource("var c: Cplx = point; var n = 0; var z = [c, 1][n];\n")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Point[0] [c]\n" +
                    "Allocate c: Cplx\n" +
                    "Assign[1] [n, 0]\n" +
                    "Allocate n: Int\n" +
                    "Switch[0] [n, 2, Label case, Label case\$1]\n" +
                    "Label case\n" +
                    "Assign[4] [z, c]\n" +
                    "Jump[0] [Label endSwitch]\n" +
                    "Label case\$1\n" +
                    "Assign[5] [z, 1.0]\n" +
                    "Jump[0] [Label endSwitch]\n" +
                    "Label endSwitch\n" +
                    "Allocate z: Cplx\n" +
                    "VarBound [c, n, z]", output)
    }

    @Test
    fun testDivExpr() {
        withSource("var a = 1; var b = a / 2; var c = 2 / a;")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "ToReal[0] [a, _]\n" +
                    "Allocate _: Real\n" +
                    "Mul[3] [0.5, _, b]\n" +
                    "Allocate b: Real\n" +
                    "ToReal[0] [a, _\$1]\n" +
                    "Allocate _\$1: Real\n" +
                    "Div[1] [2.0, _\$1, c]\n" +
                    "Allocate c: Real\n" +
                    "VarBound [a, b, c]", output)
    }

    @Test
    fun testWhileStmt() {
        withSource("var a = 1; while(a < 10) a = a + 1")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Assign[1] [a, 1]\n" +
                    "Allocate a: Int\n" +
                    "Label while\n" +
                    "Less[2] [a, 10, Label whileTrue, Label endWhile]\n" +
                    "Label whileTrue\n" +
                    "Add[1] [1, a, a]\n" +
                    "Jump[0] [Label while]\n" +
                    "Label endWhile\n" +
                    "VarBound [a]", output)
    }

    @Test
    fun testPointSetResult() {
        withSource("var a = point; setResult(1, a, 2)")

        actParse()
        actInline()
        actLinearize()

        actPrint()

        Assert.assertEquals(
            "Point[0] [a]\n" +
                    "Allocate a: Cplx\n" +
                    "SetResult[5] [1, a, 2.0]\n" +
                    "VarBound [a]", output)
    }

    private lateinit var output: String
    private lateinit var linearized: ArrayList<CodeLine>
    private lateinit var inlined: Node
    private lateinit var ast: Node
    private lateinit var stream: ParserStream

    private fun actPrint() {
        output = linearized.joinToString("\n")
    }

    private fun actLinearize() {
        linearized = ArrayList()
        val varNameGenerator = NameGenerator()
        inlined.accept(LinearizeStmt(linearized, varNameGenerator))
    }

    private fun actInline() {
        val rootTable = RootSymbolTable(FractlangProgram.namedInstructions, emptyMap())

        val varNameGenerator = NameGenerator()

        inlined = ast.accept(
            SemanticAnalysisVisitor(
                rootTable,
                varNameGenerator
            )
        )
    }

    private fun actParse() {
        ast = FractlangGrammar.program.parse(stream)!!
    }

    private fun withSource(src: String) {
        stream = ParserStream.create(src)
    }
}