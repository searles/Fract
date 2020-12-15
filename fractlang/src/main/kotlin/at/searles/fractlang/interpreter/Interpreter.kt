package at.searles.fractlang.interpreter

import at.searles.commons.math.Cplx
import at.searles.fractlang.Visitor
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

class Interpreter(private val point: Cplx, private val debugCallback: DebugCallback, private val plotCallback: PlotCallback?): Visitor<Node> {

    private val table = HashMap<String, NumValue>()
    private val activeVars = ArrayList<VarDecl>()

    var paletteIndex: Int? = null
        private set

    var colorValue: Cplx? = null
    var height: Double? = null

    fun getSymbol(id: String): NumValue? {
        return table[id]
    }

    fun getVarDeclForName(name: String): VarDecl? {
        return activeVars.find { it.name == name }
    }

    override fun visit(app: App): Node {
        require(app.head is OpNode && app.head.op is BaseOp)
        return interpret(app.trace, app.head.op, app.args)
    }

    private fun interpret(trace: Trace, op: BaseOp, args: List<Node>): Node {
        val evalArgs = args.map { it.accept(this) }

        when(op) {
            Plot -> {
                plotCallback?.plot((evalArgs[0] as CplxNode).value)
                return Nop(trace)
            }

            Next -> {
                val id = (args[1] as IdNode).id
                val currentValue = evalArgs[1] as IntNode
                val nextValue = IntNode(currentValue.trace, currentValue.value + 1)
                table[id] = nextValue

                return BoolNode(trace, nextValue.value < (args[0] as IntNode).value)
            }

            Point -> {
                return CplxNode(trace, point)
            }

            SetResult -> {
                paletteIndex = (evalArgs[0] as IntNode).value
                colorValue = (evalArgs[1] as CplxNode).value
                height = (evalArgs[2] as RealNode).value

                return Nop(trace)
            }
        }

        return op.evaluate(trace, evalArgs)
    }

    override fun visit(assignment: Assignment): Node {
        debugCallback.step(this, assignment)

        val rhs = assignment.rhs.accept(this)
        require(rhs is NumValue && assignment.lhs is IdNode)
        table[assignment.lhs.id] = rhs
        return Nop(assignment.trace)
    }

    override fun visit(idNode: IdNode): Node {
        table.containsKey(idNode.id)
        return table[idNode.id] as Node? ?: throw DebugException("uninitialized variable", idNode.trace)
    }

    override fun visit(block: Block): Node {
        var retVal: Node = Nop(block.trace)

        val activeVarsSizeBeforeBlock = activeVars.size

        block.stmts.forEach {
            require(retVal is Nop) { "failed because $retVal"}
            retVal = it.accept(this)
        }

        while(activeVars.size > activeVarsSizeBeforeBlock) {
            table.remove(activeVars[activeVars.size - 1].name)
            activeVars.removeAt(activeVars.size - 1)
        }

        return retVal
    }

    override fun visit(ifStmt: If): Node {
        debugCallback.step(this, ifStmt)

        val condition = ifStmt.condition.accept(this)

        require(condition is BoolNode)

        if(condition.value) {
            return ifStmt.thenBranch.accept(this)
        }

        return Nop(ifStmt.trace)
    }

    override fun visit(ifElse: IfElse): Node {
        debugCallback.step(this, ifElse)

        val condition = ifElse.condition.accept(this)

        require(condition is BoolNode)

        return if(condition.value) {
            ifElse.thenBranch.accept(this)
        } else {
            ifElse.elseBranch.accept(this)
        }
    }

    override fun visit(intNode: IntNode): Node {
        return intNode
    }

    override fun visit(realNode: RealNode): Node {
        return realNode
    }

    override fun visit(cplxNode: CplxNode): Node {
        return cplxNode
    }

    override fun visit(boolNode: BoolNode): Node {
        return boolNode
    }

    override fun visit(varDecl: VarDecl): Node {
        require(varDecl.init == null)
        activeVars.add(varDecl)
        return Nop(varDecl.trace)
    }

    override fun visit(whileStmt: While): Node {
        debugCallback.step(this, whileStmt)

        while(true) {
            val condition = whileStmt.condition.accept(this)
            require(condition is BoolNode)

            if(!condition.value) {
                break
            }

            whileStmt.body.accept(this)
        }

        return Nop(whileStmt.trace)
    }

    override fun visit(indexedNode: IndexedNode): Node {
        debugCallback.step(this, indexedNode)
        var index = (indexedNode.index.accept(this) as IntNode).value
        val items = (indexedNode.field as VectorNode).items

        index = (index % items.size + items.size) % items.size


        return items[index].accept(this)
    }

    override fun visit(opNode: OpNode): Node {
        debugCallback.step(this, opNode)
        when(opNode.op) {
            Point -> {
                return CplxNode(opNode.trace, point)
            }
            else -> error("unexpected op without arguments: ${opNode.op}")
        }
    }

    // -------------------------

    override fun visit(classDecl: ClassDecl): Node {
        error("unexpected in interpreter")
    }

    override fun visit(forStmt: For): Node {
        error("unexpected in interpreter")
    }

    override fun visit(funDecl: FunDecl): Node {
        error("unexpected in interpreter")
    }

    override fun visit(qualifiedNode: QualifiedNode): Node {
        error("unexpected in interpreter")
    }

    override fun visit(stringNode: StringNode): Node {
        error("unexpected in interpreter")
    }

    override fun visit(varParameter: VarParameter): Node {
        error("unexpected in interpreter")
    }

    override fun visit(vectorNode: VectorNode): Node {
        error("unexpected in interpreter")
    }

    override fun visit(classEnv: ClassEnv): Node {
        error("unexpected in interpreter")
    }

    override fun visit(funEnv: FunEnv): Node {
        error("unexpected in interpreter")
    }

    override fun visit(nop: Nop): Node {
        error("unexpected in interpreter")
    }

    override fun visit(objectNode: ObjectNode): Node {
        error("unexpected in interpreter")
    }

    override fun visit(valDecl: ValDecl): Node {
        error("unexpected in interpreter")
    }

    override fun visit(externDecl: ExternDecl): Node {
        error("unexpected in interpreter")
    }

    override fun visit(externNode: ExternNode): Node {
        error("unexpected in interpreter")
    }

    override fun visit(appChain: AppChain): Node {
        error("unexpected in interpreter")
    }
}