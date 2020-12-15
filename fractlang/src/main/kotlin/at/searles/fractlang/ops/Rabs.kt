package at.searles.fractlang.ops

import at.searles.commons.math.Cplx
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.CplxNode
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace
import kotlin.math.abs

object Rabs: StandardOp(1,
    Signature(BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args[0] is CplxNode) {
            val cplx = (args[0] as CplxNode).value
            return CplxNode(trace, Cplx(abs(cplx.re()), cplx.im()))
        }

        return createApp(trace, args)
    }
}