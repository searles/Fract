package at.searles.fractlang.vm

import at.searles.fractlang.nodes.Node

interface VmArg {
	// {IntNode, RealNode, CplxNode, IdNode, Label}
	fun vmCodeSize(): Int
	fun addToVmCode(vmCodeAssembler: VmCodeAssembler)

	interface Num: VmArg {
		override fun vmCodeSize(): Int {
			return (this as Node).type.vmCodeSize()
		}
	}
}
