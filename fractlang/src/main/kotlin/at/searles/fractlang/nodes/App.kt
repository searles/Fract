package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.fractlang.ops.Op
import at.searles.parsing.Trace

class App(trace: Trace, val head: Node, val args: List<Node>): Node(trace) {

    constructor(trace: Trace, head: Op, args: List<Node>):
            this(trace, head.toNode(trace), args)

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "$head(${args.joinToString(", ")})"
    }
}