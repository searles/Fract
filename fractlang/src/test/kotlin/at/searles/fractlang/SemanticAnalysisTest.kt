package at.searles.fractlang

import at.searles.commons.math.Scale
import at.searles.fractlang.nodes.AppChain
import at.searles.fractlang.nodes.IdNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
import org.junit.Assert
import org.junit.Test

class SemanticAnalysisTest {

    @Test
    fun testAssignWhileToVarFail() {
        withSource("var b = 1; var a = { while(b==2) {} }")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testVectorAccessConversion() {
        withSource("var a = [1, 2.0][0]; ")
        actParse()
        actInline()
        actPrint()

        Assert.assertEquals("a=1.0;vara:Real;", output)
    }

    @Test
    fun testVectorAccessNegativeIndex() {
        withSource("var a = [1, 2, 3][-1]; ")
        actParse()
        actInline()
        actPrint()

        Assert.assertEquals("a=3;vara:Int;", output)
    }

    @Test
    fun testVectorAccessTooLargeIndex() {
        withSource("var a = [1, 2, 3][3]; ")
        actParse()
        actInline()
        actPrint()

        Assert.assertEquals("a=1;vara:Int;", output)
    }
    @Test
    fun testMissingInAssignment() {
        withSource("var a = a")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testLexicalScope() {
        withSource("var a = 1; var b = {var a = 2; a}")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;b={a\$1=2;vara\$1:Int;a\$1;};varb:Int;", output)
    }

    @Test
    fun testEvalValues() {
        withSource("var a = -(-(1 + 2) - 3);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=6;vara:Int;", output)

    }

    @Test
    fun testVar() {
        withSource("var a = 1")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;", output)
    }

    @Test
    fun testRValueAssignment() {
        withSource("val a = 1; a = 1")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {}
    }

    @Test
    fun testMod0() {
        withSource("var a = 1 % 0;")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {}
    }

    @Test
    fun testModNegPos() {
        withSource("var a = -2 % 3;")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;", output)
    }

    @Test
    fun testModPosNeg() {
        withSource("var a = 2 % -3;")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=-1;vara:Int;", output)
    }

    @Test
    fun testModNegNeg() {
        withSource("var a = -2 % -3;")

        actParse()
        actInline()
        actPrint()

        Assert.assertEquals("a=-2;vara:Int;", output)
    }

    @Test
    fun testVarWithAdd() {
        withSource("var a = 1; var b = a + 1")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;b=1+a;varb:Int;", output)
    }

    @Test
    fun testVarInBlock() {
        withSource("var a = {var b = 1; b} + 1")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1+{b=1;varb:Int;b;};vara:Int;", output)
    }

    @Test
    fun testFunWithVarArg() {
        withSource("fun a(var b) {b = 2}; a(1);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=1;varb:Int;{b=2;}", output)
    }

    @Test
    fun testFunWithArgAndExpr() {
        withSource("fun a(b) = b + 1; var c = a(2);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("c=3;varc:Int;", output)
    }

    @Test
    fun testFunWithTypeVarArgAndExpr() {
        withSource("fun a(var b: Int) = b + 1; var c = a(2);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=2;varb:Int;c=1+b;varc:Int;", output)
    }

    @Test
    fun testFunWithUntypedVarArgAndExpr() {
        withSource("fun a(var b) = b + 1; var c = a(2);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=2;varb:Int;c=1+b;varc:Int;", output)
    }

    @Test
    fun testFunWithoutArgsWithBlock() {
        withSource("var a = 1; fun b() { a + 2 }; var c = b();")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;c=2+a;varc:Int;", output)
    }

    @Test
    fun testObjectWithOneMember() {
        withSource("class A { var b = 1 }; var c = A().b;")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=1;varb:Int;c=b;varc:Int;", output)
    }

    @Test
    fun testObjectWithOneUntypedArgAndOneMemberInt() {
        withSource("class A(d) { var b = d }; var c = A(1).b;")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=1;varb:Int;c=b;varc:Int;", output)
    }

    @Test
    fun testObjectWithOneUntypedArgAndOneMemberReal() {
        withSource("class A(d) { var b = d }; var c = A(1.1).b;")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=1.1;varb:Real;c=b;varc:Real;", output)
    }

    @Test
    fun testSimpleCast() {
        withSource("var x: Real = 1")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;", output)
    }

    @Test
    fun testObjectWithOneTypedArgAndOneMemberReal() {
        withSource("class A(var d: Real) { var b = d }; var c = A(1).b;")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("d=1.0;vard:Real;b=d;varb:Real;c=b;varc:Real;", output)
    }

    @Test
    fun testIfElseStmt() {
        withSource("var a = 1; if(a == 1) a = 2 else a = 3")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;if(1==a)a=2elsea=3;", output)
    }

    @Test
    fun testIfElseExpr() {
        withSource("var a = 1; a = if(a == 1) 2 else 3")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;a=if(1==a)2else3;", output)
    }

    @Test
    fun testErrorOnInconvertibleAssignment() {
        withSource("var a: Int = 1; a = 3.2;")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testErrorOnInconvertibleParameter() {
        withSource("fun f(var a: Int) { a + 1 }; var b = f(3.2);")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testErrorOnInconvertibleVarInit() {
        withSource("var a: Int = 3.2;")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testErrorOnBadNextArity() {
        withSource("var a: Int = 0; if(next(a)) a = 1;")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testPowBugDir1() {
        withSource("var a: Cplx = 0:0; var b = (a^2)^0.5")

        actParse()
        actInline()
    }

    @Test
    fun testPowBugDir2() {
        withSource("var a: Cplx = 0:0; var b = (a^0.5)^(2:2)")

        actParse()
        actInline()
    }

    @Test
    fun testDeclareStuff() {
        withSource("declareScale(1, 2, 3, 4, 5, 6); " +
                "declarePalette(\"Hi\", 1, 1, [0, 0, #ffff0000]);" +
                "declarePalette(\"Hi2\", 2, 2, [1, 1, #ffff00ff]);")

        actParse()
        actInline()

        Assert.assertNotNull(scale)
        Assert.assertEquals(2, palettes.size)
    }

    @Test
    fun testSimpleDiff() {
        withSource("var x : Real = 1; var a = diff(x + 2x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=3.0;vara:Real;", output)
    }

    @Test
    fun testAddDiff() {
        withSource("var x : Real = 1; var a = diff(log x + exp x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=/x+Exp(x);vara:Real;", output)
    }

    @Test
    fun testSubDiff() {
        withSource("var x : Real = 1; var a = diff(log x - exp x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=/x-Exp(x);vara:Real;", output)
    }

    @Test
    fun testMulDiff() {
        withSource("var x : Real = 1; var a = diff(log x * exp x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Exp(x)/x+Exp(x)*Log(x);vara:Real;", output)
    }

    @Test
    fun testDivDiff() {
        withSource("var x : Real = 1; var a = diff(log x / exp x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=(Exp(x)/x-Log(x)*Exp(x))/Exp(x)^2;vara:Real;", output)
    }

    @Test
    fun testPowNDiff() {
        withSource("var x : Real = 1; var a = diff(log x ^ 5, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Log(x)^5*(5.0/x/Log(x));vara:Real;", output)
    }


    @Test
    fun testPowDiff() {
        withSource("var x : Real = 1; var a = diff(x ^ x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=x^x*(x/x+Log(x));vara:Real;", output)
    }

    @Test
    fun testNegDiff() {
        withSource("var x : Real = 1; var a = diff(-x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=-1;vara:Int;", output)
    }

    @Test
    fun testRecipDiff() {
        withSource("var x : Real = 1; var a = diff(/cosh x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=-(Sinh(x)/Cosh(x)^2);vara:Real;", output)
    }

    @Test
    fun testSqrtDiff() {
        withSource("var x : Real = 1; var a = diff(sqrt cosh x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Sinh(x)/Sqrt(Cosh(x));vara:Real;", output)
    }

    @Test
    fun testLogDiff() {
        withSource("var x : Real = 1; var a = diff(log sin x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Cos(x)/Sin(x);vara:Real;", output)
    }

    @Test
    fun testExpDiff() {
        withSource("var x : Real = 1; var a = diff(exp sin x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Exp(Sin(x))*Cos(x);vara:Real;", output)
    }

    @Test
    fun testSinhDiff() {
        withSource("var x : Real = 1; var a = diff(sinh sin x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Cosh(Sin(x))*Cos(x);vara:Real;", output)
    }


    @Test
    fun testCoshDiff() {
        withSource("var x : Real = 1; var a = diff(cosh sin x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Sinh(Sin(x))*Cos(x);vara:Real;", output)
    }


    @Test
    fun testSinDiff() {
        withSource("var x : Real = 1; var a = diff(sin sinh x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=Cos(Sinh(x))*Cosh(x);vara:Real;", output)
    }


    @Test
    fun testCosDiff() {
        withSource("var x : Real = 1; var a = diff(cos sinh x, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=-(Sin(Sinh(x))*Cosh(x));vara:Real;", output)
    }

    @Test
    fun testDiffDiff() {
        withSource("var x : Real = 1; var a = diff(diff(cos x, x), x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=-Cos(x);vara:Real;", output)
    }

    @Test
    fun testMin3() {
        withSource("var x = max(1, 2, 3);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=3;varx:Int;", output)
    }

    @Test
    fun testMin1() {
        withSource("var x = max(1);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1;varx:Int;", output)
    }

    @Test
    fun testMin2() {
        withSource("var x = max(1, 2);")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=2;varx:Int;", output)
    }

    @Test
    fun testIfTrueElseBlock() {
        withSource("var a = 0; if(a == 1) true else { a = 2; false }")
        actParse()
        actInline()
        actPrint()
        Assert.assertEquals("a=0;vara:Int;1==aor{a=2;false;};", output)
    }

    @Test
    fun testIfFalseElseBlock() {
        withSource("var a = 0; if(a == 1) false else { a = 2; false }")
        actParse()
        actInline()
        actPrint()
        Assert.assertEquals("a=0;vara:Int;not1==aand{a=2;false;};", output)
    }

    @Test
    fun testIfElseTrueBlock() {
        withSource("var a = 0; if(a == 1) { a = 2; false } else true")
        actParse()
        actInline()
        actPrint()
        Assert.assertEquals("a=0;vara:Int;not1==aor{a=2;false;};", output)
    }

    @Test
    fun testIfElseFalseBlock() {
        withSource("var a = 0; if(a == 1) { a = 2; false } else false")
        actParse()
        actInline()
        actPrint()
        Assert.assertEquals("a=0;vara:Int;1==aand{a=2;false;};", output)
    }



    @Test
    fun testNewton() {
        withSource("var x : Real = 1; var a = newton(x^4 - 1, x)")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("x=1.0;varx:Real;a=x-(-1.0+x^4)/(x^4*(4.0/x));vara:Real;", output)
    }

    @Test
    fun testIfElseBool() {
        withSource("var a = 1; if(if(a == 1) false else true) a = 2 else a = 3")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;if(not1==a)a=2elsea=3;", output)
    }

    @Test
    fun testVectorHasNoType() {
        withSource("var c = point; var z = c + [1, -1];")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch (e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testThrowError() {
        withSource("if(true) error(\"hi\", \"this is an error\");")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch (e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testDoNotThrowError() {
        withSource("if(false) error(\"hi\", \"this is an error\");")

        actParse()

        actInline()
    }

    @Test
    fun testAddBadPalette() {
        withSource(
            "addPalette(\"p\", -1, 0, [0, 0, 0])\n")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testAddPalettes() {
        withSource(
            "addPalette(\"A\", 2, 2, [0, 0, 0]);addPalette(\"B\", 3, 3, [0, 0, 0])\n")

        actParse()
        actInline()

        Assert.assertEquals(2, palettes.size)
        Assert.assertEquals("A", palettes[0].description)
        Assert.assertEquals("B", palettes[1].description)
    }

    @Test
    fun testPutBadPalette() {
        withSource(
            "putPalette(\"p\", \"p\", -1, 0, [0, 0, 0])\n")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testPutPalettes() {
        withSource(
            "putPalette(\"A\", \"A\", 2, 2, [0, 0, 0]);putPalette(\"B\", \"B\", 3, 3, [0, 0, 0])\n")

        actParse()
        actInline()

        Assert.assertEquals(2, palettes.size)
        Assert.assertEquals("A", palettes[0].description)
        Assert.assertEquals("B", palettes[1].description)
    }

    @Test
    fun testClassNameHiding() {
        withSource(
            "var f = 2;\n" +
                    "class A(var a: Int) {\n" +
                    "    var b = a * f + 1;\n" +
                    "}\n" +
                    "{\n" +
                    "    val f = 6;\n" +
                    "    val d = A(3);\n" +
                    "    var e = d.b;\n" +
                    "}\n")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("f=2;varf:Int;{a=3;vara:Int;b=1+a*f;varb:Int;e=b;vare:Int;}", output)
    }

    @Test
    fun testClassFunCallWithParameters() {
        withSource(
            "class A(a) {\n" +
                "    fun add(b) { var c = a + b; c }\n" +
                "}\n" +
                "var f = A(1).add(2);\n")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("f={c=3;varc:Int;c;};varf:Int;", output)
    }

    private lateinit var palettes: List<PaletteEntry>
    private lateinit var scale: Scale
    private lateinit var output: String
    private lateinit var inlined: Node
    private lateinit var ast: Node
    private lateinit var stream: ParserStream

    private fun actPrint() {
        output = FractlangGrammar.program.print(inlined).toString()
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

        scale = rootTable.defaultScale
        palettes = rootTable.palettes.values.sortedBy { it.index }
    }

    private fun actParse() {
        ast = FractlangGrammar.program.parse(stream)!!
    }

    private fun withSource(src: String) {
        stream = ParserStream.create(src)
    }
}