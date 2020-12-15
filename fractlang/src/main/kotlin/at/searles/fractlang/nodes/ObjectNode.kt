package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.Visitor
import at.searles.parsing.Trace

class ObjectNode(trace: Trace, private val table: Map<String, Node>) : Node(trace) {
    init {
        type = BaseTypes.Obj
    }

    fun getMember(trace: Trace, memberId: String): Node {
        return table[memberId] ?: throw SemanticAnalysisException(
            "no such member",
            trace
        )
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "object[$table]"
    }
}
