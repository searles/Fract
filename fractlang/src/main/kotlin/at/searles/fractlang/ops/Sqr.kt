package at.searles.fractlang.ops

import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

/**
 * x^2
 */
object Sqr: Op {
    override fun apply(trace: Trace, args: List<Node>): Node {
        if(args.size != 1) {
            throw SemanticAnalysisException("'sqr' requires one argument: sqr(x)", trace)
        }

        return Pow.apply(trace,
            args[0],
            IntNode(trace, 2)
        )
    }
}