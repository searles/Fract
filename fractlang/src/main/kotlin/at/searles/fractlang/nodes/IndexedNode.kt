package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
class IndexedNode(trace: Trace, val field: Node, val index: Node) : Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "$field[$index]"
    }

    object Creator: Fold<Node, Node, Node> {
        override fun apply(stream: ParserStream, left: Node, right: Node): Node {
            return IndexedNode(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? IndexedNode)?.field
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? IndexedNode)?.index
        }

        override fun toString(): String {
            return "{index}"
        }
    }

}
