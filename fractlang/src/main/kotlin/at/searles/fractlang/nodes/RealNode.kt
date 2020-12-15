package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmCodeAssembler
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class RealNode(trace: Trace, val value: Double) : Node(trace), NumValue, VmArg.Num {
    init {
        type = BaseTypes.Real
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun addToVmCode(vmCodeAssembler: VmCodeAssembler) {
        vmCodeAssembler.add(value)
    }

    override fun isZero(): Boolean {
        return value == 0.0
    }

    override fun isOne(): Boolean {
        return value == 1.0
    }

    override fun toString(): String {
        return "$value"
    }

    object Creator: Mapping<Double, Node> { // TODO: Use BigDecimal
        override fun reduce(input: Double, stream: ParserStream): Node {
            return RealNode(stream.createTrace(), input)
        }

        override fun left(result: Node): Double? {
            return (result as? RealNode)?.value
        }

        override fun toString(): String {
            return "{real}"
        }
    }

}
