package at.searles.fractlang.ops

import at.searles.parsing.Trace
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException

class ConstOp(private val nodeFactory: (Trace) -> Node): Op {
    override fun toNode(trace: Trace): Node {
        return nodeFactory.invoke(trace)
    }

    override fun apply(trace: Trace, args: List<Node>): Node {
        throw SemanticAnalysisException("const node should not be here", trace)
    }
}