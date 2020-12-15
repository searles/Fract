package at.searles.fractlang.ops

import at.searles.commons.math.Cplx
import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

/**
 * flip(x)
 */
object Flip: Op {
    override fun apply(trace: Trace, args: List<Node>): Node {
        if(args.size != 1) {
            throw SemanticAnalysisException("'flip' requires one argument: flip(x)", trace)
        }

        return Mul.apply(trace,
            CplxNode(trace, Cplx(0.0, 1.0)),
            Conj.apply(trace, args[0])
        )
    }
}