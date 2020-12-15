package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

/**
 * The description might be modified during inlining.
 */
class ExternDecl(trace: Trace, val name: String, val description: Node, val expr: String): Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "extern $name : $description = $expr"
    }

    object Creator: Fold<Pair<String, Node>, String, Node> {
        override fun apply(stream: ParserStream, left: Pair<String, Node>, right: String): Node {
            return ExternDecl(stream.createTrace(), left.first, left.second, right)
        }

        override fun leftInverse(result: Node): Pair<String, Node>? {
            return (result as? ExternDecl)?.let {
                Pair(it.name, it.description)
            }
        }

        override fun rightInverse(result: Node): String? {
            return (result as? ExternDecl)?.let {
                it.expr
            }
        }
    }


}
