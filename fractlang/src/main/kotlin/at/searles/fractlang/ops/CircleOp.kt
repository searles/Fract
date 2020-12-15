package at.searles.fractlang.ops

import at.searles.commons.geometry.Circle
import at.searles.commons.geometry.Rect
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.CplxNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.NumValue
import at.searles.fractlang.nodes.RealNode
import at.searles.parsing.Trace

/**
 * Geometrical arcus. (center, +-radius, queryPoint)
 */
object CircleOp: StandardOp (7,
    Signature(BaseTypes.Real, BaseTypes.Cplx, BaseTypes.Real, BaseTypes.Cplx)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args.all { it is NumValue }) {
            val dist = Circle((args[0] as CplxNode).value, (args[1] as RealNode).value).dist((args[2] as CplxNode).value)
            return RealNode(trace, dist)
        }

        return createApp(trace, args)
    }
}