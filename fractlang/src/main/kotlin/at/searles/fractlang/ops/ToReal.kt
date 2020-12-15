package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.IntNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.RealNode
import at.searles.parsing.Trace

object ToReal: StandardOp(1,
    Signature(BaseTypes.Real, BaseTypes.Int)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return when(val arg = args[0]) {
            is IntNode -> RealNode(trace, arg.value.toDouble())
            else -> createApp(trace, args)
        }
    }
}