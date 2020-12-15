package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Trace

/**
 * Extern values are stored as a string. When fetched from the symbol table,
 * they become such nodes.
 */
class ExternNode(trace: Trace, val id: String, val description: String, val isDefault: Boolean, val expr: String): Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "extern[isDefault=$isDefault] $id : $description = $expr"
    }
}