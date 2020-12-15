package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.nodes.ConstValue
import at.searles.fractlang.nodes.IdNode
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

object Assign: HasSpecialSyntax, VmBaseOp(
    Signature(BaseTypes.Unit, BaseTypes.Int, BaseTypes.Int),
    Signature(BaseTypes.Unit, BaseTypes.Real, BaseTypes.Real),
    Signature(BaseTypes.Unit, BaseTypes.Cplx, BaseTypes.Cplx)
) {
    override fun getSignatureAt(offset: Int): Signature {
        return signatures[offset / 2]
    }

    override fun apply(trace: Trace, args: List<Node>): Node {
        if(args[0] !is IdNode) {
            throw SemanticAnalysisException(
                "Lhs of assignment must be a register",
                args[0].trace
            )
        }

        return super.apply(trace, args)
    }

    override fun evaluate(trace: Trace, args: List<Node>): Node {
        return createApp(trace, args)
    }

    override val countArgKinds = signatures.size * 2
    
    override fun getArgKindAt(offset: Int): List<ArgKind> {
        return with(getSignatureAt(offset)) {
            listOf(ArgKind(argTypes[0], false), ArgKind(argTypes[1], offset % 2 == 1))
        }
    }

    override fun getArgKindOffset(args: List<Node>): Int {
        val signatureIndex = signatures.indexOfFirst { it.matches(args) }

        require(signatures[signatureIndex].matchesExact(args))

        return 2 * signatureIndex + if(args[1] is ConstValue) 1 else 0
    }
}