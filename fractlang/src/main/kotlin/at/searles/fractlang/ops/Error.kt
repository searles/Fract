package at.searles.fractlang.ops

import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.Trace

object Error: MetaOp {
    override fun inlineApply(trace: Trace, args: List<Node>, visitor: SemanticAnalysisVisitor): Node {
        throw SemanticAnalysisException(args.joinToString(", "), trace)
    }
}