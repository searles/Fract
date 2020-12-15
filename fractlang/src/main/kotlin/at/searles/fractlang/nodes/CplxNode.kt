package at.searles.fractlang.nodes

import at.searles.commons.math.Cplx
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Visitor
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmCodeAssembler
import at.searles.parsing.Trace

class CplxNode(trace: Trace, val value: Cplx) : Node(trace), NumValue, VmArg.Num {
    init {
        type = BaseTypes.Cplx
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun addToVmCode(vmCodeAssembler: VmCodeAssembler) {
        vmCodeAssembler.add(value)
    }

    override fun isZero(): Boolean {
        return value.re() == 0.0 && value.im() == 0.0
    }

    override fun isOne(): Boolean {
        return value.re() == 1.0 && value.im() == 0.0
    }

    override fun toString(): String {
        return "$value"
    }
}
