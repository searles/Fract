package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class For(trace: Trace, val name: String, val range: Node, val body: Node): Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    object Creator: Fold<Pair<String, Node>, Node, Node> {
        override fun apply(stream: ParserStream, left: Pair<String, Node>, right: Node): Node {
            return For(stream.createTrace(), left.first, left.second, right)
        }

        override fun leftInverse(result: Node): Pair<String, Node>? {
            return (result as? For)?.run {
                Pair(name, range)
            }
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? For)?.body
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }

}