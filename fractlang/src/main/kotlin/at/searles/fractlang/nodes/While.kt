package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class While(trace: Trace, val condition: Node, val body: Node): Node(trace) {
    init {
        type = BaseTypes.Unit
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "while($condition) $body"
    }

    object Creator: Fold<Node, Node, Node> {

        override fun apply(stream: ParserStream, left: Node, right: Node): Node {
            return While(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? While)?.condition
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? While)?.body
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }
}