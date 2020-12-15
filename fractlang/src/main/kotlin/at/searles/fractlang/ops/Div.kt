package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.semanticanalysis.Optimizer
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Div: HasSpecialSyntax, StandardOp (2,
    Signature(BaseTypes.Real, BaseTypes.Real, BaseTypes.Real),
    Signature(BaseTypes.Cplx, BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return Optimizer.div(trace, args)
    }
}