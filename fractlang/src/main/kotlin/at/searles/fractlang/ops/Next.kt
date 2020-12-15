package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

/**
 * next(a, b) is equivalent to C++ (++b < a), thus a must not be const.
 */
object Next: StandardOp(2,
    Signature(BaseTypes.Bool, BaseTypes.Int, BaseTypes.Int)
) {
    override fun apply(trace: Trace, args: List<Node>): Node {
        if(args.size != 2) {
            throw SemanticAnalysisException("next has arity 2", trace)
        }

        if(args[1] !is IdNode) {
            throw SemanticAnalysisException(
                "2nd argument in next must be a register",
                args[1].trace
            )
        }

        return super.apply(trace, args)
    }

    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return createApp(trace, args)
    }
}