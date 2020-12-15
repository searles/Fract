package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

/**
 * The inlined and semantically analyzed code can use this function.
 */
object Plot: BaseOp(Signature(BaseTypes.Unit, BaseTypes.Cplx)) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        // Keep as is.
        return createApp(trace, args)
    }
}