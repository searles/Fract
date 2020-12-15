package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.IntNode
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

/**
 * switch index size labels...
 */
object Switch: VmBaseOp(Signature(BaseTypes.Unit, BaseTypes.Int, BaseTypes.Int)) {
    override val countArgKinds: Int
        get() = 1

    override fun getArgKindAt(offset: Int): List<ArgKind> {
        return listOf(ArgKind(BaseTypes.Int, false), ArgKind(BaseTypes.Int, true))
    }

    override fun getArgKindOffset(args: List<Node>): Int {
        require(args[0].type == BaseTypes.Int && args[0] !is IntNode)
        require(args[1] is IntNode)

        return 0
    }

    override fun getSignatureAt(offset: Int): Signature {
        return signatures[0]
    }

    override fun evaluate(trace: Trace, args: List<Node>): Node {
        error("must only be used during linearization")
    }
}