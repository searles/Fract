package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class Assignment(trace: Trace, val lhs: Node, val rhs: Node): Node(trace) {

    init {
        type = BaseTypes.Unit
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "$lhs = $rhs"
    }

    object Creator: Fold<Node, Node, Node> {
        override fun apply(stream: ParserStream, left: Node, right: Node): Node {
            return Assignment(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? Assignment)?.lhs
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? Assignment)?.rhs
        }

        override fun toString(): String {
            return "{set}"
        }
    }

}