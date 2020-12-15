package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.parsing.Initializer
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

/**
 * This node represents a no-operation node and is a null-object for nodes.
 */
class Nop(trace: Trace) : Node(trace) {

    init {
        type = BaseTypes.Unit
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "NOP"
    }

    object Creator: Initializer<Node> {
        override fun parse(stream: ParserStream): Node {
            return Nop(stream.createTrace())
        }

        override fun consume(t: Node): Boolean {
            return t is Nop
        }

        override fun toString(): String {
            return "{nop}"
        }
    }

}
