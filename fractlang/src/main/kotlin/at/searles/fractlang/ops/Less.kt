package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.parsing.Trace

object Less: HasSpecialSyntax, StandardOp(3,
    Signature(BaseTypes.Bool, BaseTypes.Int, BaseTypes.Int),
    Signature(BaseTypes.Bool, BaseTypes.Real, BaseTypes.Real)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args.all { it is NumValue }) {
            return when (val arg0 = args[0]) {
                is IntNode -> BoolNode(trace, arg0.value < (args[1] as IntNode).value)
                is RealNode -> BoolNode(trace, arg0.value < (args[1] as RealNode).value)
                else -> error("bug")
            }
        }

        return createApp(trace, args)
    }
}