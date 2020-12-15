package at.searles.fractlang.ops

import at.searles.parsing.Trace
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.OpNode
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor

interface MetaOp {
    fun inlineApply(trace: Trace, args: List<Node>, visitor: SemanticAnalysisVisitor): Node

    fun toNode(trace: Trace): Node {
        return OpNode(trace, this)
    }
}