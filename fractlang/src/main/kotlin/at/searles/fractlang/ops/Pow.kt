package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.Optimizer
import at.searles.parsing.Trace

object Pow: HasSpecialSyntax, StandardOp(3,
    Signature(BaseTypes.Real, BaseTypes.Real, BaseTypes.Int),
    Signature(BaseTypes.Real, BaseTypes.Real, BaseTypes.Real),
    Signature(BaseTypes.Cplx, BaseTypes.Cplx, BaseTypes.Int),
    Signature(BaseTypes.Cplx, BaseTypes.Cplx, BaseTypes.Real),
    Signature(BaseTypes.Cplx, BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return Optimizer.pow(trace, args)
    }
}