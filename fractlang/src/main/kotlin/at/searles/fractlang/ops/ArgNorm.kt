package at.searles.fractlang.ops

import at.searles.commons.math.Cplx
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.parsing.Trace
import kotlin.math.PI

object ArgNorm: StandardOp (1,
    Signature(BaseTypes.Real, BaseTypes.Cplx)
) {
    private const val tau = 2 * PI

    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return if(args[0] is CplxNode)
            RealNode(trace, calc((args[0] as CplxNode).value))
        else
            createApp(trace, args[0])
    }

    private fun calc(c: Cplx): Double {
        val arg = c.arg() / tau

        return if(arg < 0) arg + 1 else arg
    }
}