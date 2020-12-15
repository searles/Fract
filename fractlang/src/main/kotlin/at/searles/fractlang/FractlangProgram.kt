package at.searles.fractlang

import at.searles.commons.math.Cplx
import at.searles.fractlang.interpreter.Interpreter
import at.searles.fractlang.linear.CodeLine
import at.searles.fractlang.linear.LinearizeStmt
import at.searles.fractlang.nodes.CplxNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.RealNode
import at.searles.fractlang.ops.*
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.fractlang.vm.VmCodeAssembler
import at.searles.parsing.ParserStream

@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate", "unused")
class FractlangProgram(val sourceCode: String, val customParameters: Map<String, String>) {

    private val symbolTable = RootSymbolTable(namedInstructions, customParameters)
    private val varNameGenerator = NameGenerator()

    private lateinit var typedAst: Node
    private lateinit var linearizedCode: ArrayList<CodeLine>
    private lateinit var vmCodeAssembler: VmCodeAssembler

    val vmCode
        get() = vmCodeAssembler.vmCode.toIntArray()

    val defaultScale
            get() = symbolTable.defaultScale

    val defaultPalettes
            get() = symbolTable.palettes

    val activeParameters
            get() = symbolTable.activeParameters

    init {
        analyze()
        compile()
    }

    fun runInterpreter(interpreter: Interpreter) {
        typedAst.accept(interpreter)
    }

    private fun analyze() {
        val sourceCodeStream = ParserStream.create(sourceCode)

        val ast = FractlangGrammar.program.parse(sourceCodeStream)
            ?: throw SemanticAnalysisException("Could not parse program", sourceCodeStream.createTrace())

        if(!FractlangGrammar.eof.recognize(sourceCodeStream)) {
            throw SemanticAnalysisException("Program not fully parsed", sourceCodeStream.createTrace())
        }

        typedAst = ast.accept(SemanticAnalysisVisitor(symbolTable, varNameGenerator))

        if(typedAst.type != BaseTypes.Unit) {
            throw SemanticAnalysisException("Program must be of type 'Unit'", typedAst.trace)
        }
    }

    private fun compile(): FractlangProgram {
        linearizedCode = ArrayList()
        typedAst.accept(LinearizeStmt(linearizedCode, varNameGenerator))
        vmCodeAssembler = VmCodeAssembler(linearizedCode, vmInstructions)

        return this
    }

    companion object {
        val vmInstructions: List<VmBaseOp> = listOf(
            Add, Sub, Mul, Div, Mod, Pow, Neg,
            Recip, Abs, Assign, Jump, Equal, Less, Next, Switch,
            Sqrt, Exp, Log, Sin, Cos, Tan, Asin, Acos, Atan,
            Sinh, Cosh, Tanh, Asinh, Acosh, Atanh,
            ToReal, Cons,
            Arg, ArgNorm, RealPart, ImagPart, Conj, Cabs, Rabs, Iabs, Norm,
            Point, SetResult,
            Max, Min, Floor, Fract,
            ArcOp, LineOp, CircleOp, RectOp
        )

        val namedInstructions = mapOf(
            "next" to Next,
            "abs" to Abs,
            "neg" to Neg,
            "rec" to Recip,
            "sqrt" to Sqrt,
            "exp" to Exp,
            "log" to Log,
            "log1p" to Log1P,
            "argnorm" to ArgNorm,
            "sin" to Sin,
            "cos" to Cos,
            "atan" to Atan,
            "sinh" to Sinh,
            "cosh" to Cosh,
            "conj" to Conj,
            "rad" to Abs, // legacy due to beta version
            "rabs" to Rabs,
            "iabs" to Iabs,
            "re" to RealPart,
            "im" to ImagPart,
            "cabs" to Cabs,
            "arg" to Arg,
            "point" to Point,
            "max" to Max,
            "min" to Min,
            "norm" to Norm,
            "floor" to Floor,
            "setResult" to SetResult,
            "toReal" to ToReal,
            "pi" to ConstOp { RealNode(it, Math.PI) },
            "tau" to ConstOp { RealNode(it, 2 * Math.PI) },
            "e" to ConstOp { RealNode(it, Math.E) },
            "i" to ConstOp { CplxNode(it, Cplx(0.0, 1.0)) },
            "diff" to Diff,
            "newton" to Newton,
            "setScale" to SetScale,
            "putPalette" to PutPalette,
            "addPalette" to AddPalette, // legacy due to usage up to version 1.0.3
            "declareScale" to SetScale, // legacy due to beta version
            "declarePalette" to DeclarePalette, // legacy due to beta version
            "line" to LineOp,
            "rect" to RectOp,
            "circle" to CircleOp,
            "arc" to ArcOp,
            "sqr" to Sqr,
            "flip" to Flip,
            "fract" to Fract,
            "tan" to Tan,
            "tanh" to Tanh,
            "atanh" to Atanh,
            "asin" to Asin,
            "asinh" to Asinh,
            "acos" to Acos,
            "acosh" to Acosh,
            "error" to Error,
            "plot" to Plot
        )
    }
}