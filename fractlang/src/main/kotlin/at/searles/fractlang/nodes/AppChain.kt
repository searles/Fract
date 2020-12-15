package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.fractlang.ops.Op
import at.searles.parsing.Fold
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class AppChain(trace: Trace, val left: Node, val right: List<Node>): Node(trace) {

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "$left (${right.joinToString(", ")})"
    }

    object Creator: Fold<Node, List<Node>, Node> {
        override fun apply(stream: ParserStream, left: Node, right: List<Node>): Node {
            return AppChain(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? AppChain)?.left
        }

        override fun rightInverse(result: Node): List<Node>? {
            return (result as? AppChain)?.right
        }

        override fun toString(): String {
            return "{app-chain}"
        }
    }
}