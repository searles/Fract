package at.searles.fractlang.ops

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

object Newton: Op {
    override fun apply(trace: Trace, args: List<Node>): Node {
        if(args.size != 2) {
            throw SemanticAnalysisException("'newton' requires two arguments: newton(f, var)", trace)
        }

        return Sub.apply(trace,
            args[1],
            Div.apply(trace,
                args[0],
                Diff.apply(trace, args)
            )
        )
    }
}