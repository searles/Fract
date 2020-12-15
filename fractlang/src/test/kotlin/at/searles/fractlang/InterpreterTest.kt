package at.searles.fractlang

import at.searles.commons.math.Cplx
import at.searles.fractlang.interpreter.DebugCallback
import at.searles.fractlang.interpreter.Interpreter
import at.searles.fractlang.interpreter.PlotCallback
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.ParserStream
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.FileReader

class InterpreterTest {
    @Test
    fun testNotify() {
        withSource("""var a = 1; plot (a); plot(a + i);""")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(Cplx(1.0, 0.0), plottedValues[0])
        Assert.assertEquals(Cplx(1.0, 1.0), plottedValues[1])
    }

    @Test
    fun testThenExpr() {
        withSource("var a = 1; var b = if(a == 1) 1 else 2; setResult(b, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(1, interpreter.paletteIndex)
    }

    @Test
    fun testElseExpr() {
        withSource("var a = 0; var b = if(a == 1) 1 else 2; setResult(b, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(2, interpreter.paletteIndex)
    }

    @Test
    fun testIfStmt() {
        withSource("var a = 1; var b = 0; if(a == 1) b = 1; setResult(b, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(1, interpreter.paletteIndex)
    }

    @Test
    fun testIfStmtNotExecuted() {
        withSource("var a = 0; var b = 0; if(a == 1) b = 1; setResult(b, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(0, interpreter.paletteIndex)
    }

    @Test
    fun testLoop() {
        withSource("var a = 1; var sum = 0; while(a < 10) { sum = sum + a; a = a + 1 }; setResult(sum, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(45, interpreter.paletteIndex)
    }

    @Test
    fun testInterruptEndlessLoop() {
        withSource("var a = 1; var sum = 0; while(a > 0) { a = a + 1 };")

        actParse()
        actInline()

        val bgThread = Thread(Runnable { actInterpret() }).apply {
            start()
        }

        Thread.sleep(10)

        Assert.assertEquals(Thread.State.RUNNABLE, bgThread.state)

        isStopped = true

        Thread.sleep(10)

        Assert.assertEquals(Thread.State.TERMINATED, bgThread.state)
    }

    @Test
    fun testNextLoop() {
        withSource("var a = 1; var sum = 0; while (next(10, a)) { sum = sum + a }; setResult(sum, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(44, interpreter.paletteIndex)
    }

    @Test
    fun testAddition() {
        withSource("var a = 1; var b = a + 2; setResult(b, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(3, interpreter.paletteIndex)
    }

    @Test
    fun testOneVar() {
        withSource("var a = 1; setResult(a, 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(1, interpreter.paletteIndex)
    }

    @Test
    fun testIndexed() {
        withSource("var a = 1; setResult([1,2,3][a], 0, 0)")

        actParse()
        actInline()
        actInterpret()

        Assert.assertEquals(2, interpreter.paletteIndex)
    }

    @Test
    fun testSimpleMandel() {
        val filename = "src/test/resources/mandelbrot.ft"
        withSource(FileReader(filename).readText())
        point = Cplx(-1.0, 0.0)

        actParse()
        actInline()

        actInterpret()

        Assert.assertEquals(22505, countDebugSteps)
        Assert.assertEquals(2500, plottedValues.size)
    }

    private lateinit var inlined: Node
    private lateinit var ast: Node
    private lateinit var stream: ParserStream
    private lateinit var interpreter: Interpreter
    private lateinit var plottedValues: ArrayList<Cplx>
    private var point: Cplx = Cplx(0.0, 0.0)
    private var countDebugSteps = 0
    private var isStopped = false

    @Before
    fun setUp() {
        plottedValues = ArrayList()
        countDebugSteps = 0
        isStopped = false
    }

    private fun actInterpret(): Node {
        interpreter = Interpreter(point,
            object: DebugCallback {
                override fun step(interpreter: Interpreter, node: Node) {
                    countDebugSteps++
                    if(isStopped) {
                        throw Exception()
                    }
                }
            },
            object: PlotCallback {
                override fun plot(z: Cplx) {
                    plottedValues.add(z)
                }
            } 
        )
        return inlined.accept(interpreter)
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