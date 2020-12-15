package at.searles.fractlang.ops

import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

/**
 * log(x + 1)
 */
object Log1P: Op {
    override fun apply(trace: Trace, args: List<Node>): Node {
        if(args.size != 1) {
            throw SemanticAnalysisException("'log1p' requires one argument: log1p(x)", trace)
        }

        return Log.apply(trace,
            Add.apply(trace,
                RealNode(trace, 1.0),
                args[0]
            )
        )
    }
}