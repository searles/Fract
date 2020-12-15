package at.searles.fractlang.ops

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.Nop
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.Trace

/**
 * Due to definition in beta version.
 */
object DeclarePalette: MetaOp {
    override fun inlineApply(trace: Trace, args: List<Node>, visitor: SemanticAnalysisVisitor): Node {
        AddPalette.inlineApply(trace, args, visitor)
        return Nop(trace)
    }
}