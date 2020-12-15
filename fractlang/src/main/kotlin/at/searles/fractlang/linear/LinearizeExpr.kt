package at.searles.fractlang.linear

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.NameGenerator
import at.searles.fractlang.Visitor
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.*
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmInstruction

class LinearizeExpr(private val code: ArrayList<CodeLine>, private val nameGenerator: NameGenerator, private val optTargetNode: IdNode?): Visitor<VmArg> {

    private fun createIdNode(node: Node): IdNode {
        return IdNode(node.trace, nameGenerator.next("_")).apply { this.type = node.type }
    }

    override fun visit(app: App): VmArg {
        require(app.head is OpNode)
        require(app.head.op is BaseOp)

        val op = app.head.op as VmBaseOp

        val index = op.getArgKindOffset(app.args)

        val linearizedArgs = app.args.map { it.accept(
            LinearizeExpr(
                code,
                nameGenerator,
                null
            )
        )}

        val target = optTargetNode ?: createIdNode(app)

        // last argument is the target.
        code.add(VmInstruction(op, index, linearizedArgs + target))

        if(optTargetNode == null) {
            code.add(Alloc(target.id, target.type))
        }

        return target
    }

    override fun visit(ifElse: IfElse): VmArg {
        val target = optTargetNode ?: createIdNode(ifElse)

        // last argument is the target.
        val trueLabel = Label(nameGenerator.next("ifElseTrue"))
        val falseLabel = Label(nameGenerator.next("ifElseFalse"))
        val endLabel = Label(nameGenerator.next("ifElseEnd"))

        ifElse.condition.accept(LinearizeBool(code, nameGenerator, trueLabel, falseLabel))
        code.add(trueLabel)
        ifElse.thenBranch.accept(LinearizeExpr(code, nameGenerator, target))
        code.add(VmInstruction(Jump, 0, listOf(endLabel)))
        code.add(falseLabel)
        ifElse.elseBranch.accept(LinearizeExpr(code, nameGenerator, target))
        code.add(endLabel)

        if(optTargetNode == null) {
            code.add(Alloc(target.id, target.type))
        }

        return target
    }

    override fun visit(block: Block): VmArg {
        require(block.stmts.isNotEmpty())

        block.stmts.dropLast(1).forEach {
            it.accept(LinearizeStmt(code, nameGenerator))
        }

        code.add(VarBound(block.stmts.dropLast(1).filterIsInstance<VarDecl>().map {
            IdNode(it.trace, it.name).apply { type = it.varType!! }
        }))

        // Always use a register for returns
        val target = optTargetNode ?: createIdNode(block)

        // Return value is 'target'.
        block.stmts.last().accept(LinearizeExpr(code, nameGenerator, target))

        if(optTargetNode == null) {
            code.add(Alloc(target.id, target.type))
        }

        return target
    }

    override fun visit(idNode: IdNode): VmArg {
        if(optTargetNode != null && optTargetNode.id != idNode.id) {
            return assignIfTargetNode(idNode)
        }

        return idNode
    }

    private fun assignIfTargetNode(arg: VmArg): VmArg {
        if(optTargetNode != null) {
            require(optTargetNode.type == (arg as Node).type)

            val convArg = optTargetNode.type.convert(arg as Node)
            val args =  listOf(optTargetNode, convArg)

            require(convArg is VmArg)

            @Suppress("UNCHECKED_CAST")
            code.add(
                VmInstruction(
                    Assign,
                    Assign.getArgKindOffset(args),
                    args as List<VmArg>
                )
            )

            return optTargetNode
        }

        return arg
    }

    override fun visit(intNode: IntNode): VmArg {
        return assignIfTargetNode(intNode)
    }

    override fun visit(realNode: RealNode): VmArg {
        return assignIfTargetNode(realNode)
    }

    override fun visit(cplxNode: CplxNode): VmArg {
        return assignIfTargetNode(cplxNode)
    }

    override fun visit(opNode: OpNode): VmArg {
        return visit(App(opNode.trace, opNode, emptyList()).apply { type = (opNode.op as BaseOp).signatures[0].returnType })
    }

    override fun visit(indexedNode: IndexedNode): VmArg {
        require(indexedNode.index.type == BaseTypes.Int) {"index must be an int" }
        require(indexedNode.field is VectorNode) {"field must be a vector"}

        // TODO: Do I need alloc here?

        val targetNode = optTargetNode ?: IdNode(indexedNode.trace, nameGenerator.next("_")).apply {
            type = indexedNode.type
        }

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
            it.first.accept(LinearizeExpr(code, nameGenerator, targetNode))
            code.add(jumpToEnd)
        }

        code.add(endLabel)

        return targetNode
    }

    override fun visit(qualifiedNode: QualifiedNode): VmArg {
        error("not applicable")
    }

    override fun visit(boolNode: BoolNode): VmArg {
        error("not applicable")
    }

    override fun visit(varDecl: VarDecl): VmArg {
        error("not applicable")
    }

    override fun visit(forStmt: For): VmArg {
        error("not applicable")
    }

    override fun visit(ifStmt: If): VmArg {
        error("not applicable")
    }

    override fun visit(whileStmt: While): VmArg {
        error("not applicable") // should be a type error.
    }

    override fun visit(classEnv: ClassEnv): VmArg {
        error("not applicable")
    }

    override fun visit(funEnv: FunEnv): VmArg {
        error("not applicable")
    }

    override fun visit(objectNode: ObjectNode): VmArg {
        error("not applicable")
    }

    override fun visit(valDecl: ValDecl): VmArg {
        error("not applicable")
    }

    override fun visit(nop: Nop): VmArg {
        error("not applicable")
    }

    override fun visit(classDecl: ClassDecl): VmArg {
        error("not applicable")
    }

    override fun visit(funDecl: FunDecl): VmArg {
        error("not applicable")
    }

    override fun visit(stringNode: StringNode): VmArg {
        error("not applicable")
    }

    override fun visit(varParameter: VarParameter): VmArg {
        error("not applicable")
    }

    override fun visit(vectorNode: VectorNode): VmArg {
        error("not applicable")
    }

    override fun visit(assignment: Assignment): VmArg {
        error("not applicable")
    }

    override fun visit(externDecl: ExternDecl): VmArg {
        error("not applicable")
    }

    override fun visit(externNode: ExternNode): VmArg {
        error("not applicable")
    }

    override fun visit(appChain: AppChain): VmArg {
        error("not applicable")
    }
}
