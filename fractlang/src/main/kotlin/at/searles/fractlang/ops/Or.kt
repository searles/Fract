package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.BoolNode
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Or: HasSpecialSyntax, BaseOp(
    Signature(BaseTypes.Bool, BaseTypes.Bool, BaseTypes.Bool)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        for (i in 0 .. 1) {
            if (args[i] is BoolNode) {
                return if((args[i] as BoolNode).value)
                    args[i]
                else
                    args[1 - i]
            }
        }

        return createApp(trace, args)
    }
}