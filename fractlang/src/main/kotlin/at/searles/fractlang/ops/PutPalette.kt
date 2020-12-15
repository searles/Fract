package at.searles.fractlang.ops

import at.searles.commons.color.Lab
import at.searles.commons.color.Palette
import at.searles.commons.color.Rgb
import at.searles.commons.util.IntIntMap
import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.Trace

/**
 * Should not be used anymore.
 */
object PutPalette: MetaOp {
    override fun inlineApply(trace: Trace, args: List<Node>, visitor: SemanticAnalysisVisitor): Node {
        val inlinedArgs = args.map { it.accept(visitor) }

        val label = (inlinedArgs[0] as? StringNode)?.value ?: throw SemanticAnalysisException("label in arg[0] must be a string", args[0].trace)
        val description = (inlinedArgs[1] as? StringNode)?.value ?: throw SemanticAnalysisException("description in arg[1] must be a string", args[1].trace)
        val width = (inlinedArgs[2] as? IntNode)?.value ?: throw SemanticAnalysisException("width in arg[2] must be an integer", args[2].trace)
        val height = (inlinedArgs[3] as? IntNode)?.value ?: throw SemanticAnalysisException("height in arg[3] must be an integer", args[3].trace)

        if(width <= 0) {
            throw SemanticAnalysisException("width must be > 0", trace)
        }

        if(height <= 0) {
            throw SemanticAnalysisException("height must be > 0", trace)
        }

        val colorMap = IntIntMap<Lab>()

        val points = args.drop(4).map { toColorPoint(it, width, height) }

        points.forEach {
            colorMap[it[0], it[1]] = Rgb.of(it[2]).toLab()
        }

        if(points.isEmpty()) {
            throw SemanticAnalysisException("Palette must have at least one color point", trace)
        }

        val index = visitor.table.putPalette(trace, label, description, Palette(width, height, 0f, 0f, colorMap))

        return IntNode(trace, index)
    }

    private fun toColorPoint(node: Node, width: Int, height: Int): IntArray {
        if(node !is VectorNode || node.items.size != 3) {
            throw SemanticAnalysisException("not a vector", node.trace)
        }

        node.items.forEach {
            if(it !is IntNode) {
                throw SemanticAnalysisException("not an integer", it.trace)
            }
        }

        val pt = IntArray(3) {(node.items[it] as IntNode).value}

        if(pt[0] < 0 || width <= pt[0]) {
            throw SemanticAnalysisException("Not in range of palette", node.items[0].trace)
        }

        if(pt[1] < 0 || height <= pt[1]) {
            throw SemanticAnalysisException("Not in range of palette", node.items[0].trace)
        }

        return pt
    }
}