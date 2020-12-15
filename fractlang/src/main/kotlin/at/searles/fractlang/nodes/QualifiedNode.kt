package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
class QualifiedNode(trace: Trace, val instance: Node, val qualifier: String) : Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "$instance.$qualifier"
    }

    object Creator: Fold<Node, String, Node> {
        override fun apply(stream: ParserStream, left: Node, right: String): Node {
            return QualifiedNode(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? QualifiedNode)?.instance
        }

        override fun rightInverse(result: Node): String? {
            return (result as? QualifiedNode)?.qualifier
        }

        override fun toString(): String {
            return "{qualified}"
        }
    }
}
