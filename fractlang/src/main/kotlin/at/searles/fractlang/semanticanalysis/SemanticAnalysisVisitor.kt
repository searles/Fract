package at.searles.fractlang.semanticanalysis

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.NameGenerator
import at.searles.fractlang.SymbolTable
import at.searles.fractlang.Visitor
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.And
import at.searles.fractlang.ops.Not
import at.searles.fractlang.ops.Or
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class SemanticAnalysisVisitor(parentTable: SymbolTable, val varNameGenerator: NameGenerator): Visitor<Node> {
	val block = ArrayList<Node>()
    val table = parentTable.fork()

	fun setInTable(trace: Trace, id: String, value: Node) {
        if(!table.set(id, value)) {
            throw SemanticAnalysisException(
				"already defined",
				trace
			)
        }
	}

	fun addStmt(stmt: Node) {
		if(stmt !is Nop) {
			block.add(stmt)
		}
	}
	
	/**
	 * @param trace The trace of the whole declaration
	 * @param initialization optional assigned value. Must be inlined.
	 * @param varNode The declaration part. Must contain the proper type.
	 */
	fun initializeVar(trace: Trace, varNode: VarParameter, initialization: Node?) {
		val type = varNode.varType
				?: initialization?.type
				?: throw SemanticAnalysisException(
					"Could not determine type",
					varNode.trace
				)

		if(type.vmCodeSize() <= 0) {
			throw SemanticAnalysisException("Cannot declare a variable of this type", trace)
		}

		val newVarName = varNameGenerator.next(varNode.name)
		val newIdNode = IdNode(varNode.trace, newVarName).apply {
			this.type = type
		}

		if(initialization != null) {
			val assignment = Assignment(trace, newIdNode, type.convert(initialization))
			addStmt(assignment)
		}

		val newVarDecl = VarDecl(trace, newVarName, type.toString(), null)
		addStmt(newVarDecl)

		setInTable(varNode.trace, varNode.name, newIdNode)
	}


    override fun visit(funDecl: FunDecl): Node {
        setInTable(funDecl.trace, funDecl.name, FunEnv(funDecl, table))
        return Nop(funDecl.trace)
    }

    override fun visit(classDecl: ClassDecl): Node {
        setInTable(classDecl.trace, classDecl.name, ClassEnv(classDecl, table))
        return Nop(classDecl.trace)
    }

	override fun visit(opNode: OpNode): Node {
		return opNode
	}

	override fun visit(app: App): Node {
		val head = app.head.accept(this)
		return head.accept(
			SemanticAnalysisAppVisitor(
				app.trace,
				app.args,
				this
			)
        )
    }

	override fun visit(appChain: AppChain): Node {
		val head = appChain.left.accept(this)
		return head.accept(
			SemanticAnalysisAppVisitor(
				appChain.trace,
				appChain.right,
				this
			)
		)
	}

	override fun visit(qualifiedNode: QualifiedNode): Node {
        val instance = qualifiedNode.instance.accept(this)

        if(instance is ObjectNode) {
			// It is an object or a complex number
			return instance.getMember(qualifiedNode.trace, qualifiedNode.qualifier)
		}

		throw SemanticAnalysisException(
			"not an object",
			qualifiedNode.trace
		)
    }

	override fun visit(valDecl: ValDecl): Node {
		val assignment = valDecl.init.accept(this)
		table.set(valDecl.name, assignment)
		return Nop(valDecl.trace)
	}

	override fun visit(varDecl: VarDecl): Node {
        val initialization = varDecl.init?.accept(this)

        val type = varDecl.varType
            ?:initialization?.type
            ?:throw SemanticAnalysisException(
				"missing type",
				varDecl.trace
			)

		if(type.vmCodeSize() == 0) {
			throw SemanticAnalysisException(
				"cannot create variable of type $type",
				varDecl.trace
			)
		}

		initializeVar(varDecl.trace,
			VarParameter(varDecl.trace, varDecl.name, type.toString()), initialization)
		
        return Nop(varDecl.trace)
    }

    override fun visit(idNode: IdNode): Node {
		val node = table[idNode.trace, idNode.id]
			?: throw SemanticAnalysisException("${idNode.id} is undefined", idNode.trace)

		// Injected value?
		if(node is ExternNode) {
			return node.accept(this)
		}

		return node
	}

    override fun visit(block: Block): Node {
        val innerVisitor = SemanticAnalysisVisitor(table, varNameGenerator)
		
		block.stmts.forEach {
			innerVisitor.addStmt(it.accept(innerVisitor))
		}

		val stmts = innerVisitor.block

		if(stmts.size == 1 && stmts.last().type != BaseTypes.Unit) {
			return stmts.last()
		}

		return Block(block.trace, stmts.filter { it !is Nop }).apply {
			type = if(stmts.isEmpty()) BaseTypes.Unit else stmts.last().type
		}
    }

    override fun visit(classEnv: ClassEnv): Node {
        return classEnv.accept(
			SemanticAnalysisAppVisitor(
				classEnv.trace,
				emptyList(),
				this
			)
		)
    }

    override fun visit(funEnv: FunEnv): Node {
        return funEnv.accept(
			SemanticAnalysisAppVisitor(
				funEnv.trace,
				emptyList(),
				this
			)
		)
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

    override fun visit(stringNode: StringNode): Node {
		return stringNode
    }

    override fun visit(ifElse: IfElse): Node {
		val inlinedCondition = ifElse.condition.accept(this)
		
		if(inlinedCondition.type != BaseTypes.Bool) {
			throw SemanticAnalysisException(
				"boolean expected",
				inlinedCondition.trace
			)
		}

		if(inlinedCondition is BoolNode) {
			return if(inlinedCondition.value) {
				ifElse.thenBranch.accept(this)
			} else {
				ifElse.elseBranch.accept(this)
			}
		}

		val inlinedThenBranch = ifElse.thenBranch.accept(this)
		val inlinedElseBranch = ifElse.elseBranch.accept(this)

		val type = inlinedThenBranch.type.commonType(inlinedElseBranch.type) ?:
			throw SemanticAnalysisException(
				"incompatible types in if-else statement",
				ifElse.trace
			)



		if(inlinedThenBranch is BoolNode) {
			// if(a) true else b

			//      a | b      || result | b.isExecuted
			//  false | false  || false  | true
			//  false | true   || true   | true
			//   true | false  || true   | false
			//   true | true   || true   | false

			// ==> a or b

			// if(a) false else b

			//      a | b      || result | b.isExecuted
			//  false | false  || false  | true
			//  false | true   || true   | true
			//   true | false  || false  | false
			//   true | true   || false  | false

			// ==> not a and b

			return if(inlinedThenBranch.value) {
				Or.apply(ifElse.trace, listOf(inlinedCondition, inlinedElseBranch))
			} else {
				And.apply(ifElse.trace, listOf(
					Not.apply(ifElse.trace, listOf(inlinedCondition)),
					inlinedElseBranch)
				)
			}
		}

		if(inlinedElseBranch is BoolNode) {
			// if(a) b else true

			//      a | b      || result | b.isExecuted
			//  false | false  || true   | false
			//  false | true   || true   | false
			//   true | false  || false  | true
			//   true | true   || true   | true

			// ==> not a or b

			// if(a) b else false

			//      a | b      || result | b.isExecuted
			//  false | false  || false  | false
			//  false | true   || false  | false
			//   true | false  || false  | true
			//   true | true   || true   | true

			// ==> a and b
			return if(inlinedElseBranch.value) {
				Or.apply(ifElse.trace, listOf(
					Not.apply(ifElse.trace, listOf(inlinedCondition)),
					inlinedThenBranch)
				)
			} else {
				And.apply(ifElse.trace, listOf(inlinedCondition, inlinedThenBranch))
			}
		}

		return IfElse(
			ifElse.trace,
			inlinedCondition,
			type.convert(inlinedThenBranch),
			type.convert(inlinedElseBranch)
		).apply {
				this.type = type
		}
    }

    override fun visit(ifStmt: If): Node {
		val inlinedCondition = ifStmt.condition.accept(this)
		
		if(inlinedCondition.type != BaseTypes.Bool) {
			throw SemanticAnalysisException(
				"boolean expected",
				inlinedCondition.trace
			)
		}

		if(inlinedCondition is BoolNode) {
			if(!inlinedCondition.value) {
				return Nop(ifStmt.trace)
			}

			return ifStmt.thenBranch.accept(this)
		}

		val inlinedBody = ifStmt.thenBranch.accept(this)
		
		if(inlinedBody.type != BaseTypes.Unit) {
			throw SemanticAnalysisException(
				"no return type expected",
				inlinedBody.trace
			)
		}
		
		return If(ifStmt.trace, inlinedCondition, inlinedBody)
    }

    override fun visit(whileStmt: While): Node {
		val inlinedCondition = whileStmt.condition.accept(this)
		
		if(inlinedCondition.type != BaseTypes.Bool) {
			throw SemanticAnalysisException(
				"boolean expected",
				inlinedCondition.trace
			)
		}

		val inlinedBody = whileStmt.body.accept(this)
		
		if(inlinedBody.type != BaseTypes.Unit) {
			throw SemanticAnalysisException(
				"no return type expected",
				inlinedBody.trace
			)
		}
		
		if(inlinedCondition is BoolNode) {
			if(inlinedCondition.value) {
				throw SemanticAnalysisException(
					"infinite loop",
					whileStmt.trace
				)
			}
			
			return Block(whileStmt.trace, listOf()).apply { type = BaseTypes.Unit }
		}
		
		return While(whileStmt.trace, inlinedCondition, inlinedBody)
    }

	override fun visit(nop: Nop): Node {
		return Block(nop.trace, listOf()).apply { type = BaseTypes.Unit }
	}

	override fun visit(objectNode: ObjectNode): Node {
		// XXX is it?
		throw SemanticAnalysisException("I thought this branch is unreachable. Please file a bug with " +
				"the source code.", objectNode.trace)
	}

    override fun visit(vectorNode: VectorNode): Node {
		val inlinedItems = vectorNode.items.map { it.accept(this) }
		return VectorNode(vectorNode.trace, inlinedItems)
    }

	override fun visit(indexedNode: IndexedNode): Node {
		val inlinedIndex = indexedNode.index.accept(this)

		if(inlinedIndex.type != BaseTypes.Int) {
			throw SemanticAnalysisException("Index must be an integer but was ${inlinedIndex.type}", inlinedIndex.trace)
		}

		val inlinedField = indexedNode.field.accept(this)

		if(inlinedField !is VectorNode) {
			throw SemanticAnalysisException("Not a vector", inlinedField.trace)
		}

		if(inlinedField.items.isEmpty()) {
			throw SemanticAnalysisException("Vector must not be empty", inlinedField.trace)
		}

		val commonType = inlinedField.items.fold(inlinedField.items.first().type) { type, item ->
			type.commonType(item.type) ?: throw SemanticAnalysisException(
				"${item.type} cannot be converted to $type", item.trace
			)
		}

		val typedInlinedField = VectorNode(inlinedField.trace, inlinedField.items.map { commonType.convert(it) })

		if(inlinedIndex is IntNode) {
			val size = typedInlinedField.items.size
			val index = ((inlinedIndex.value % size) + size) % size
			return typedInlinedField.items[index]
		}

		return IndexedNode(indexedNode.trace, typedInlinedField, inlinedIndex).apply { type = commonType }
	}

	override fun visit(forStmt: For): Node {
		// syntax: for ( (var)? id (':' type)? in collection )
		// collection is either a vector or a range.
		// XXX: Needed?
		throw SemanticAnalysisException(
			"not implemented",
			forStmt.trace
		)
    }

    override fun visit(varParameter: VarParameter): Node {
		error("not applicable")
    }

	override fun visit(assignment: Assignment): Node {
		val lhs = assignment.lhs.accept(this)

		if(lhs !is IdNode) {
			throw SemanticAnalysisException(
				"Cannot assign to this value",
				lhs.trace
			)
		}

		if(lhs.type.vmCodeSize() <= 0) {
			throw SemanticAnalysisException(
				"Not a variable",
				lhs.trace
			)
		}

		val rhs = assignment.rhs.accept(this)

		if(!lhs.type.canConvert(rhs)) {
			throw SemanticAnalysisException("Cannot convert to ${lhs.type}", rhs.trace)
		}

		return Assignment(assignment.trace, lhs, lhs.type.convert(rhs))
	}

	override fun visit(externDecl: ExternDecl): Node {
		val description = externDecl.description.accept(this)

		if(description !is StringNode) {
			throw SemanticAnalysisException("Description of extern must be a string", description.trace)
		}

		table.addExternValue(externDecl.trace, externDecl.name, description.value, externDecl.expr)
		return Nop(externDecl.trace)
	}

	override fun visit(externNode: ExternNode): Node {
		try {
			val stream = ParserStream.create(externNode.expr)
			val exprAst = FractlangGrammar.expr.parse(stream)
				?: throw SemanticAnalysisException("Could not parse extern value", externNode.trace)

			if(!FractlangGrammar.eof.recognize(stream)) {
				throw SemanticAnalysisException("Extern value not fully parsed", externNode.trace)
			}

			return exprAst.accept(SemanticAnalysisVisitor(AllowImplicitExternsFacade(externNode.id, externNode.trace, this), varNameGenerator))
// TODO		} catch(e: BacktrackNotAllowedException) {
//			throw SemanticAnalysisException("Unexpected token. Expected ${e.failedParser.right}", externNode.trace)
		} catch (e: SemanticAnalysisException) {
			throw SemanticAnalysisException("Semantic error in extern value: ${e.message}", externNode.trace)
		}
	}

}
