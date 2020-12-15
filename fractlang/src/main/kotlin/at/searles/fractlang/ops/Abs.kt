package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.semanticanalysis.Optimizer
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Abs: HasSpecialSyntax, StandardOp(1,
    Signature(BaseTypes.Int, BaseTypes.Int),
    Signature(BaseTypes.Real, BaseTypes.Real),
    Signature(BaseTypes.Real, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return Optimizer.abs(trace, args)
    }
}