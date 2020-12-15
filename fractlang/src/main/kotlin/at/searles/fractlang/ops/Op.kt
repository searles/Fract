package at.searles.fractlang.ops

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.Trace

interface Op: MetaOp {
    override fun inlineApply(trace: Trace, args: List<Node>, visitor: SemanticAnalysisVisitor): Node {
        return apply(trace, args.map { it.accept(visitor) })
    }

    fun apply(trace: Trace, vararg args: Node): Node {
        return apply(trace, listOf(*args))
    }

    fun apply(trace: Trace, args: List<Node>): Node
}
