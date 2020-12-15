package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.parsing.Trace

/**
 * Requires as input a layer
 */
object SetResult: StandardOp(8, Signature(BaseTypes.Unit, BaseTypes.Int, BaseTypes.Cplx, BaseTypes.Real)) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return createApp(trace, args)
    }
}