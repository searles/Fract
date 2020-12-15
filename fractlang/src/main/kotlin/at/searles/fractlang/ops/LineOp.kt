package at.searles.fractlang.ops

import at.searles.commons.geometry.Line
import at.searles.commons.geometry.Rect
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.CplxNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.NumValue
import at.searles.fractlang.nodes.RealNode
import at.searles.parsing.Trace

/**
 * Geometrical arcus. (from, to, queryPoint)
 */
object LineOp: StandardOp (7,
    Signature(BaseTypes.Real, BaseTypes.Cplx, BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args.all { it is NumValue }) {
            val dist = Line((args[0] as CplxNode).value, (args[1] as CplxNode).value).dist((args[2] as CplxNode).value)
            return RealNode(trace, dist)
        }

        return createApp(trace, args)
    }
}