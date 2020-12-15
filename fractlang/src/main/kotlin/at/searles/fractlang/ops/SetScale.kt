package at.searles.fractlang.ops

import at.searles.commons.math.Scale
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.Nop
import at.searles.fractlang.nodes.RealNode
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.Trace

object SetScale: MetaOp {
    override fun inlineApply(trace: Trace, args: List<Node>, visitor: SemanticAnalysisVisitor): Node {
        if(args.size != 6) {
            throw SemanticAnalysisException("declareScale must have 6 parameters", trace)
        }

        val inlinedArgs = args.map { it.accept(visitor) }

        val scaleList =  inlinedArgs.map { (BaseTypes.Real.convert(it) as RealNode).value }

        visitor.table.setScale(trace, toScale(scaleList))

        return Nop(trace)
    }

    private fun toScale(array: List<Double>): Scale {
        return Scale(array[0], array[1], array[2], array[3], array[4], array[5])
    }
}