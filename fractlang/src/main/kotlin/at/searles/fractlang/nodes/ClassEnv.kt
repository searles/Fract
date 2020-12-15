package at.searles.fractlang.nodes

import at.searles.fractlang.SymbolTable
import at.searles.fractlang.Visitor

class ClassEnv(val decl: ClassDecl, val table: SymbolTable) : Node(decl.trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "$decl"
    }
}
