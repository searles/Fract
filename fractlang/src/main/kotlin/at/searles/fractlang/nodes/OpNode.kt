package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.fractlang.ops.BaseOp
import at.searles.fractlang.ops.MetaOp
import at.searles.parsing.Trace

class OpNode(trace: Trace, val op: MetaOp) : Node(trace) {

    init {
        type = if(op is BaseOp && op.signatures.size == 1 && op.signatures[0].argTypes.isEmpty()) {
            op.signatures[0].returnType
        } else {
            BaseTypes.Unit
        }
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return op.javaClass.simpleName
    }
}
