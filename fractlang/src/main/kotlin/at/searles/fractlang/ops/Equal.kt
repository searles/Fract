package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.parsing.Trace

object Equal: HasSpecialSyntax, StandardOp(2,
    Signature(BaseTypes.Bool, BaseTypes.Int, BaseTypes.Int)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args.all{ it is ConstValue }) {
            return BoolNode(trace, (args[0] as IntNode).value == (args[1] as IntNode).value)
        }

        if(args[1] is ConstValue) {
            return createApp(trace, listOf(args[1], args[0]))
        }

        return createApp(trace, args)
    }
}