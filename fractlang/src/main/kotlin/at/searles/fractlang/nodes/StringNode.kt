package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
class StringNode(trace: Trace, val value: String): Node(trace) {
    init {
        type = BaseTypes.Obj
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "\"$value\""
    }

    object Creator: Mapping<String, Node> {
        override fun reduce(left: String, stream: ParserStream): Node {
            return StringNode(stream.createTrace(), left)
        }

        override fun left(result: Node): String? {
            return (result as? StringNode)?.value
        }

        override fun toString(): String {
            return "{string}"
        }
    }

}