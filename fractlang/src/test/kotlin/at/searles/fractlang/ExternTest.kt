package at.searles.fractlang

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.ParserStream
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ExternTest {
    @Test
    fun testSimpleExtern() {
        withSource(
            "extern a: \"A\" = \"1\"; var b = a")

        actParse()
        actInline()

        actPrint()

        Assert.assertEquals("b=1;varb:Int;", output)
    }

    @Test
    fun testExternBugWithNext() {
        withSource(
            "var b: Cplx = 0; extern a: \"A\" = \"b + next(1, b)\"; var c = a")

        actParse()

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testInlinedExtern() {
        withSource(
            "var a = 1; extern e: \"A\" = \"if(c < a) d else 4 \"; var b = e")

        actParse()

        actAddExtern("d", "f + g")
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;b=if(0<a)0else4;varb:Int;", output)

        Assert.assertTrue(rootTable.activeParameters.containsKey("c"))
        Assert.assertTrue(rootTable.activeParameters.containsKey("d"))

    }

    @Test
    fun testTransitiveInlinedExtern() {
        withSource(
            "var a = 1; extern e: \"A\" = \"if(c < a) d else 4 \"; var b = e")

        actParse()

        actAddExtern("d", "f + 17")
        actInline()

        actPrint()

        Assert.assertEquals("a=1;vara:Int;b=if(0<a)17else4;varb:Int;", output)

        Assert.assertTrue(rootTable.activeParameters.containsKey("c"))
        Assert.assertTrue(rootTable.activeParameters.containsKey("f"))
    }

    @Test
    fun testExternOrderSimple() {
        withSource(
            "var a = 1; extern e: \"A\" = \"if(c < a) d else 4 \"; var b = e")

        actParse()

        actAddExtern("d", "f + 17")
        actInline()

        actPrint()

        Assert.assertEquals(listOf("e", "c", "d", "f"), rootTable.activeParameters.keys.toList())
    }

    @Test
    fun testExternInExtern() {
        withSource(
            "extern a: \"A\" = \"{extern b:\\\"B\\\"=\\\"0\\\"; b}\"; var c = a")

        actParse()

        actAddExtern("b", "a")

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testExternInExternOrder() {
        withSource(
            "var i = 0; extern a: \"A\" = \"{extern b:\\\"B\\\"=\\\"0\\\"; b}\"; var c = a")

        actParse()

        actInline()
        actPrint()

        Assert.assertEquals(listOf("a", "b"), rootTable.activeParameters.keys.toList())
    }

    @Test
    fun testExternWithImplicitOrder() {
        withSource(
            "extern a: \"A\" = \"b\"; extern c: \"C\" = \"d\"; var e = a; var f = c")

        actParse()

        actInline()
        actPrint()

        Assert.assertEquals(listOf("a", "b", "c", "d"), rootTable.activeParameters.keys.toList())
    }

    @Test
    fun testInactiveExtern() {
        withSource(
            "extern a: \"A\" = \"b\"; var c = 0")

        actParse()

        actInline()
        actPrint()

        Assert.assertTrue(rootTable.activeParameters.isEmpty())
    }

    @Test
    fun testExternHiding() {
        withSource(
            "extern a: \"A\" = \"false\";\n" +
                    "var b = if(a) {" +
                    "    extern b: \"B\" = \"1\"; b" +
                    "} else 2;")

        actParse()
        actInline()

        Assert.assertEquals(1, rootTable.activeParameters.size)
    }

    @Test
    fun testSelfReference() {
        withSource(
            "extern a: \"A\" = \"if(c < a) d else 4 \"; var b = a")

        actParse()

        actAddExtern("d", "f + 17")

        try {
            actInline()
            Assert.fail()
        } catch(e: SemanticAnalysisException) {
            e.printStackTrace()
        }
    }

    @Test
    fun testDescriptionSeparate() {
        withSource("val descA = \"Some very long text that explains a.\";" +
                "extern a: descA = \"1\"; " +
                "var b = a;")

        actParse()
        actInline()

        Assert.assertTrue(rootTable.activeParameters.containsKey("a"))
        Assert.assertTrue(rootTable.activeParameters.getValue("a").description == "Some very long text that explains a.")
    }

    private lateinit var output: String
    private lateinit var inlined: Node
    private lateinit var ast: Node
    private lateinit var stream: ParserStream
    private lateinit var rootTable: RootSymbolTable
    private lateinit var externs: HashMap<String, String>

    @Before
    fun setUp() {
        externs = HashMap()
    }

    private fun actAddExtern(name: String, value: String) {
        externs[name] = value
    }

    private fun actPrint() {
        output = FractlangGrammar.program.print(inlined).toString()
    }

    private fun actInline() {
        rootTable = RootSymbolTable(FractlangProgram.namedInstructions, externs)
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