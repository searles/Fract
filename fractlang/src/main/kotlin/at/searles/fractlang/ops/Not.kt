package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.BoolNode
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Not: HasSpecialSyntax, BaseOp(Signature(BaseTypes.Bool, BaseTypes.Bool)) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args[0] is BoolNode) return BoolNode(trace, !(args[0] as BoolNode).value)
        return createApp(trace, args)
    }
}