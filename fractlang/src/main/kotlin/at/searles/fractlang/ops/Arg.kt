package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.parsing.Trace

object Arg: StandardOp (1,
    Signature(BaseTypes.Real, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return if(args[0] is CplxNode)
            RealNode(trace, (args[0] as CplxNode).value.arg())
        else
            createApp(trace, args)
    }
}