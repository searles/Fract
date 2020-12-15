package at.searles.fractlang.ops

import at.searles.commons.math.Cplx
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.CplxNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.RealNode
import at.searles.parsing.Trace
import kotlin.math.tanh

object Tanh: StandardOp (1,
    Signature(BaseTypes.Real, BaseTypes.Real),
    Signature(BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return when {
            args[0] is RealNode -> RealNode(trace, tanh((args[0] as RealNode).value))
            args[0] is CplxNode -> CplxNode(trace, Cplx().tanh((args[0] as CplxNode).value))
            else -> createApp(trace, args)
        }
    }
}