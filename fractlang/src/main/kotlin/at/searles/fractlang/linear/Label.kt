package at.searles.fractlang.linear

import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmCodeAssembler

class Label(val id: String): VmArg, CodeLine {

	override fun vmCodeSize(): Int = 1

	override fun addToVmCode(vmCodeAssembler: VmCodeAssembler) {
		vmCodeAssembler.addLabel(id)
	}

	override fun toString(): String {
		return "Label $id"
	}
}
