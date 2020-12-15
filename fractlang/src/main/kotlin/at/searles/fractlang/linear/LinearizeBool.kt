package at.searles.fractlang.linear

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.NameGenerator
import at.searles.fractlang.Visitor
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.*
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmInstruction

class LinearizeBool(private val code: ArrayList<CodeLine>, private val nameGenerator: NameGenerator, private val trueLabel: Label, private val falseLabel: Label): Visitor<Unit> {
    override fun visit(boolNode: BoolNode) {
        if(boolNode.value) {
            code.add(VmInstruction(Jump, 0, listOf(trueLabel)))
        } else {
            code.add(VmInstruction(Jump, 0, listOf(falseLabel)))
        }
    }

    private fun visitAnd(args: List<Node>) {
        val midLabel = Label(nameGenerator.next("and"))
        args[0].accept(LinearizeBool(code, nameGenerator, midLabel, falseLabel))
        code.add(midLabel)
        args[1].accept(LinearizeBool(code, nameGenerator, trueLabel, falseLabel))
    }

    private fun visitOr(args: List<Node>) {
        val midLabel = Label(nameGenerator.next("or"))
        args[0].accept(LinearizeBool(code, nameGenerator, trueLabel, midLabel))
        code.add(midLabel)
        args[1].accept(LinearizeBool(code, nameGenerator, trueLabel, falseLabel))
    }

    private fun visitXor(args: List<Node>) {
        val midTrueLabel = Label(nameGenerator.next("xorTrue"))
        val midFalseLabel = Label(nameGenerator.next("xorFalse"))
        args[0].accept(LinearizeBool(code, nameGenerator, midTrueLabel, midFalseLabel))
        code.add(midTrueLabel)
        args[1].accept(LinearizeBool(code, nameGenerator, falseLabel, trueLabel))
        code.add(midFalseLabel)
        args[1].accept(LinearizeBool(code, nameGenerator, trueLabel, falseLabel))
    }

    private fun visitNot(args: List<Node>) {
        args[0].accept(LinearizeBool(code, nameGenerator, falseLabel, trueLabel))
    }

    private fun visitExpr(op: VmBaseOp, args: List<Node>) {
        val index = op.getArgKindOffset(args)
        val linearizedArgs = args.map {
            it.accept(LinearizeExpr(code, nameGenerator, null))
        }

        code.add(
            VmInstruction(
                op,
                index,
                linearizedArgs + trueLabel + falseLabel
            )
        )
    }

    override fun visit(app: App) {
        require(app.head is OpNode && app.head.op is BaseOp)

        when(app.head.op) {
            is And -> visitAnd(app.args)
            is Or -> visitOr(app.args)
            is Xor -> visitXor(app.args)
            is Not -> visitNot(app.args)
            else -> {
                require(app.head.op is VmBaseOp)
                visitExpr(app.head.op, app.args)
            }
        }
    }

    override fun visit(block: Block) {
        require(block.stmts.isNotEmpty())

        block.stmts.dropLast(1).forEach {
            it.accept(LinearizeStmt(code, nameGenerator))
        }

        code.add(VarBound(block.stmts.dropLast(1).filterIsInstance<VarDecl>().map {
            IdNode(it.trace, it.name).apply { type = it.varType!! }
        }))

        return block.stmts.last().accept(this)
    }

    override fun visit(ifElse: IfElse) {
        val condTrueLabel = Label(nameGenerator.next("ifElseTrue"))
        val condFalseLabel = Label(nameGenerator.next("ifElseFalse"))

        ifElse.condition.accept(LinearizeBool(code, nameGenerator, condTrueLabel, condFalseLabel))
        code.add(condTrueLabel)
        ifElse.thenBranch.accept(this)
        code.add(condFalseLabel)
        ifElse.elseBranch.accept(this)
    }

    override fun visit(opNode: OpNode) {
        return visit(App(opNode.trace, opNode, emptyList()).apply { type = (opNode.op as BaseOp).signatures[0].returnType })
    }

    override fun visit(indexedNode: IndexedNode) {
        // XXX 100% same as the one in LinearizeStmt.

        require(indexedNode.index.type == BaseTypes.Int) {"index must be an int" }
        require(indexedNode.field is VectorNode) {"field must be a vector"}

        val index = indexedNode.index.accept(LinearizeExpr(code, nameGenerator, null))
        val size = IntNode(indexedNode.field.trace, indexedNode.field.items.size)

        val itemsWithLabels: List<Pair<Node, Label>> =
            indexedNode.field.items.map { Pair(it, Label(nameGenerator.next("case"))) }

        val args: List<VmArg> = listOf(index, size) + itemsWithLabels.map { it.second }

        val endLabel = Label(nameGenerator.next("endSwitch"))

        val jumpToEnd = VmInstruction(Jump, 0, listOf(endLabel))

        code.add(VmInstruction(Switch, 0, args))

        itemsWithLabels.forEach {
            val label = it.second
            code.add(label)
            it.first.accept(this)
            code.add(jumpToEnd)
        }

        code.add(endLabel)
    }

    override fun visit(classDecl: ClassDecl) {
        error("not applicable")
    }

    override fun visit(forStmt: For) {
        error("not applicable")
    }

    override fun visit(funDecl: FunDecl) {
        error("not applicable")
    }

    override fun visit(idNode: IdNode) {
        error("not applicable")
    }

    override fun visit(ifStmt: If) {
        error("not applicable")
    }

    override fun visit(intNode: IntNode) {
        error("not applicable")
    }

    override fun visit(qualifiedNode: QualifiedNode) {
        error("not applicable")
    }

    override fun visit(realNode: RealNode) {
        error("not applicable")
    }

    override fun visit(stringNode: StringNode) {
        error("not applicable")
    }

    override fun visit(varDecl: VarDecl) {
        error("not applicable")
    }

    override fun visit(varParameter: VarParameter) {
        error("not applicable")
    }

    override fun visit(vectorNode: VectorNode) {
        error("not applicable")
    }

    override fun visit(whileStmt: While) {
        error("not applicable")
    }

    override fun visit(classEnv: ClassEnv) {
        error("not applicable")
    }

    override fun visit(funEnv: FunEnv) {
        error("not applicable")
    }

    override fun visit(cplxNode: CplxNode) {
        error("not applicable")
    }

    override fun visit(nop: Nop) {
        error("not applicable")
    }

    override fun visit(objectNode: ObjectNode) {
        error("not applicable")
    }

    override fun visit(valDecl: ValDecl) {
        error("not applicable")
    }

    override fun visit(assignment: Assignment) {
        error("not applicable")
    }

    override fun visit(externDecl: ExternDecl) {
        error("not applicable")
    }

    override fun visit(externNode: ExternNode) {
        error("not applicable")
    }

    override fun visit(appChain: AppChain) {
        error("not applicable")
    }
}
