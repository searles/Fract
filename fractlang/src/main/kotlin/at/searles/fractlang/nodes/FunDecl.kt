package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
class FunDecl(trace: Trace, val name: String, val parameters: List<Node>, val body: Node): Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "fun $name($parameters) $body"
    }

    object Creator: Fold<Pair<String, List<Node>>, Node, Node> {
        override fun apply(stream: ParserStream, left: Pair<String, List<Node>>, right: Node): Node {
            return FunDecl(stream.createTrace(), left.first, left.second, right)
        }

        override fun leftInverse(result: Node): Pair<String, List<Node>>? {
            return (result as? FunDecl)?.let {
                Pair(it.name, it.parameters)
            }
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? FunDecl)?.let {
                it.body
            }
        }

        override fun toString(): String {
            return javaClass.simpleName
        }

    }

}