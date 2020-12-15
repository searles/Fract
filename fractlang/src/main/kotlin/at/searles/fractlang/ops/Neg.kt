package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.Optimizer
import at.searles.parsing.Trace

object Neg: HasSpecialSyntax, StandardOp(1,
    Signature(BaseTypes.Int, BaseTypes.Int),
    Signature(BaseTypes.Real, BaseTypes.Real),
    Signature(BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return Optimizer.neg(trace, args)
    }
}