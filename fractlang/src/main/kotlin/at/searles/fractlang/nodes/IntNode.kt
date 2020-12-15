package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmCodeAssembler
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
import java.math.BigInteger

class IntNode(trace: Trace, val value: Int) : Node(trace), NumValue, VmArg.Num {
    init {
        type = BaseTypes.Int
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun isZero(): Boolean {
        return value == 0
    }

    override fun isOne(): Boolean {
        return value == 1
    }

    override fun addToVmCode(vmCodeAssembler: VmCodeAssembler) {
        vmCodeAssembler.add(value)
    }

    override fun toString(): String {
        return "$value"
    }

    object Creator: Mapping<BigInteger, Node> {
        override fun reduce(input: BigInteger, stream: ParserStream): Node {
            val intValue = input.toInt()

            if(BigInteger.valueOf(intValue.toLong()) != input) {
                throw SemanticAnalysisException("integer must be in range -2147483648 to 2147483647", stream.createTrace())
            }

            return IntNode(stream.createTrace(), input.toInt())
        }

        override fun left(result: Node): BigInteger? {
            return (result as? IntNode)?.value?.let { BigInteger.valueOf(it.toLong()) }
        }

        override fun toString(): String {
            return "{int}"
        }
    }
}
