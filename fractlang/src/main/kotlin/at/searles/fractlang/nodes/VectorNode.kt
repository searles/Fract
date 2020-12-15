package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

/**
 * Is removed during semantic analysis?
 */
class VectorNode(trace: Trace, val items: List<Node>): Node(trace) {

    init {
        type = BaseTypes.Obj
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "[${items.joinToString(", ")}]"
    }

    object Creator: Mapping<List<Node>, Node> {
        override fun reduce(left: List<Node>, stream: ParserStream): Node {
            return VectorNode(stream.createTrace(), left)
        }

        override fun left(result: Node): List<Node>? {
            return (result as? VectorNode)?.items
        }

        override fun toString(): String {
            return "{vector}"
        }
    }

}
