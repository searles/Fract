package at.searles.fractlang.linear

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.NameGenerator
import at.searles.fractlang.Visitor
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.*
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmInstruction

class LinearizeStmt(private val code: ArrayList<CodeLine>, private val nameGenerator: NameGenerator): Visitor<Unit> {

    override fun visit(app: App) {
        require(app.head is OpNode)
        if(app.head.op == Plot) return
        require(app.head.op is VmBaseOp)

        val op: VmBaseOp = app.head.op

        val opOffset = op.getArgKindOffset(app.args)

        require(opOffset < op.countArgKinds)

        val linearizedArgs = app.args.map { it.accept(LinearizeExpr(code, nameGenerator, null))}
        code.add(
            VmInstruction(
                op,
                opOffset,
                linearizedArgs
            )
        )
    }

    override fun visit(assignment: Assignment) {
        require(assignment.lhs is IdNode)
        assignment.rhs.accept(LinearizeExpr(code, nameGenerator, assignment.lhs))
    }

    override fun visit(block: Block) {
        block.stmts.forEach {
            it.accept(this)
        }

        code.add(VarBound(block.stmts.filterIsInstance<VarDecl>().map {
            IdNode(it.trace, it.name).apply { type = it.varType!! }
        }))
    }

    override fun visit(varDecl: VarDecl) {
        require(varDecl.init == null && varDecl.varType != null && varDecl.varType!!.vmCodeSize() > 0)
        code.add(Alloc(varDecl.name, varDecl.varType!!))
    }

    override fun visit(idNode: IdNode) {
        error("should be caught during semantic analysis")
    }

    override fun visit(forStmt: For) {
        error("should be caught during semantic analysis")
    }

    override fun visit(ifStmt: If) {
        val trueLabel = Label(nameGenerator.next("ifTrue"))
        val falseLabel = Label(nameGenerator.next("ifFalse"))

        ifStmt.condition.accept(LinearizeBool(code, nameGenerator, trueLabel, falseLabel))
        code.add(trueLabel)
        ifStmt.thenBranch.accept(this)
        code.add(falseLabel)
    }

    override fun visit(ifElse: IfElse) {
        val trueLabel = Label(nameGenerator.next("ifElseTrue"))
        val falseLabel = Label(nameGenerator.next("ifElseFalse"))
        val endLabel = Label(nameGenerator.next("endIfElse"))

        ifElse.condition.accept(LinearizeBool(code, nameGenerator, trueLabel, falseLabel))
        code.add(trueLabel)
        ifElse.thenBranch.accept(this)
        code.add(VmInstruction(Jump, 0, listOf(endLabel)))
        code.add(falseLabel)
        ifElse.elseBranch.accept(this)
        code.add(endLabel)
    }

    override fun visit(whileStmt: While) {
        val startLabel = Label(nameGenerator.next("while"))
        val trueLabel = Label(nameGenerator.next("whileTrue"))
        val falseLabel = Label(nameGenerator.next("endWhile"))

        code.add(startLabel)

        whileStmt.condition.accept(
            LinearizeBool(
                code,
                nameGenerator,
                trueLabel,
                falseLabel
            )
        )

        code.add(trueLabel)
        whileStmt.body.accept(this)
        code.add(VmInstruction(Jump, 0, listOf(startLabel)))
        code.add(falseLabel)
    }

    override fun visit(opNode: OpNode) {
        return visit(App(opNode.trace, opNode, emptyList()).apply { type = (opNode.op as BaseOp).signatures[0].returnType })
    }

    override fun visit(indexedNode: IndexedNode) {
        // XXX 100% same as the one in LinearizeBool.

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

    override fun visit(intNode: IntNode) {
        error("not applicable")
    }

    override fun visit(realNode: RealNode) {
        error("not applicable")
    }

    override fun visit(vectorNode: VectorNode) {
        error("not applicable")
    }

    override fun visit(cplxNode: CplxNode) {
        error("not applicable")
    }

    override fun visit(boolNode: BoolNode) {
        error("not applicable")
    }

    override fun visit(stringNode: StringNode) {
        error("not applicable")
    }

    override fun visit(classDecl: ClassDecl) {
        error("not applicable")
    }

    override fun visit(funDecl: FunDecl) {
        error("not applicable")
    }

    override fun visit(qualifiedNode: QualifiedNode) {
        error("not applicable")
    }

    override fun visit(varParameter: VarParameter) {
        error("not applicable")
    }

    override fun visit(valDecl: ValDecl) {
        error("not applicable")
    }

    override fun visit(nop: Nop) {
        error("not applicable")
    }

    override fun visit(objectNode: ObjectNode) {
        error("not applicable")
    }
    override fun visit(classEnv: ClassEnv) {
        error("not applicable")
    }

    override fun visit(funEnv: FunEnv) {
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