package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.nodes.IntNode
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Jump: VmBaseOp(
    Signature(BaseTypes.Unit, BaseTypes.Int)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return createApp(trace, args)
    }

    override val countArgKinds = 1

    override fun getArgKindAt(offset: Int): List<ArgKind> {
        return listOf(ArgKind(BaseTypes.Int, true))
    }

    override fun getArgKindOffset(args: List<Node>): Int {
        require(args[0] is IntNode)
        return 0
    }

    override fun getSignatureAt(offset: Int): Signature {
        return signatures[0]
    }

    override fun apply(trace: Trace, args: List<Node>): Node {
        throw SemanticAnalysisException(
            "cannot apply jump at this stage",
            trace
        )
    }
}