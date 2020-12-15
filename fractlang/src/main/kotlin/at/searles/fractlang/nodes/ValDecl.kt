package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class ValDecl(trace: Trace, val name: String, val init: Node): Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "val $name = $init"
    }

    object Creator: Fold<String, Node, Node> {
        override fun apply(stream: ParserStream, left: String, right: Node): Node {
            return ValDecl(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): String? {
            return (result as? ValDecl)?.name
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? ValDecl)?.init
        }

        override fun toString(): String {
            return javaClass.simpleName
        }

    }
}