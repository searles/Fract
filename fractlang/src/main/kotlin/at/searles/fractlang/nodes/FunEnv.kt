package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.SymbolTable
import at.searles.fractlang.Visitor

class FunEnv(val decl: FunDecl, val table: SymbolTable) : Node(decl.trace) {

    init {
        type = BaseTypes.Unit
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return decl.toString()
    }
}
