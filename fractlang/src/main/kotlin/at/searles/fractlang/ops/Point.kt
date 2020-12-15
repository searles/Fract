package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Point: VmBaseOp(Signature(BaseTypes.Cplx)) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return createApp(trace, args)
    }

    override val countArgKinds = 1

    override fun getArgKindAt(offset: Int): List<ArgKind> {
        return emptyList()
    }

    override fun getArgKindOffset(args: List<Node>): Int {
        return 0
    }

    override fun getSignatureAt(offset: Int): Signature {
        return signatures[0]
    }
}