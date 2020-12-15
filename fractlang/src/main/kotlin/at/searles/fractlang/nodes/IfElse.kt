package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.parsing.Fold
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
class IfElse(trace: Trace, val condition: Node, val thenBranch: Node, val elseBranch: Node): Node(trace) {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "if($condition) $thenBranch else $elseBranch"
    }

    object Creator: Fold<Node, Node, Node> {
        override fun apply(stream: ParserStream, left: Node, right: Node): Node {
            return IfElse(stream.createTrace(), (left as If).condition, left.thenBranch, right)
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? IfElse)?.run {
                If(trace, condition, thenBranch)
            }
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? IfElse)?.elseBranch
        }

        override fun toString(): String {
            return javaClass.simpleName
        }

    }

}