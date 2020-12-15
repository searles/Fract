package at.searles.fractlang.vm

import at.searles.commons.math.Cplx
import at.searles.fractlang.linear.Alloc
import at.searles.fractlang.linear.CodeLine
import at.searles.fractlang.linear.Label
import at.searles.fractlang.linear.VarBound
import at.searles.fractlang.nodes.IdNode
import at.searles.fractlang.ops.Assign
import at.searles.fractlang.ops.BaseOp
import at.searles.fractlang.ops.VmBaseOp
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class VmCodeAssembler(private val linearizedCode: ArrayList<CodeLine>, instructions: Collection<VmBaseOp>) {

	private val instructionOffsets = createInstructionOffsets(instructions)

	private val memoryOffsets = HashMap<String, Int>()

	private val labelOffsets = HashMap<String, Int>()

	val vmCode = ArrayList<Int>()
	
	init {
		initializeMemoryOffsets()
		linearizedCode.removeIf { isSelfAssignment(it) }
		initializeLabelOffsets()
		linearizedCode.filterIsInstance<VmInstruction>().forEach { it.addToVmCode(this) }
	}

	private fun isSelfAssignment(line: CodeLine): Boolean {
		if(line is VmInstruction && line.op == Assign && line.args[1] is IdNode) {
			require(memoryOffsets.contains((line.args[0] as IdNode).id))
			return memoryOffsets[(line.args[0] as IdNode).id] == memoryOffsets[(line.args[1] as IdNode).id]
		}

		return false
	}

	fun add(code: Int) {
		vmCode.add(code)
	}

	fun add(real: Double) {
		val l = java.lang.Double.doubleToRawLongBits(real)
		vmCode.add((l and 0x0ffffffffL).toInt())
		vmCode.add((l shr 32).toInt())
	}

	fun add(cplx: Cplx) {
		add(cplx.re())
		add(cplx.im())
	}
	
	fun addVar(id: String) {
		add(memoryOffsets[id] ?: error("not in memory: $id"))
	}

	fun addLabel(id: String) {
		add(labelOffsets[id] ?: error("label not assigned: $id"))
	}

	private fun initializeLabelOffsets() {
		var offset = 0
		linearizedCode.forEach {
			if(it is VmInstruction) {
				offset += it.vmCodeSize()
			} else if(it is Label) {
				labelOffsets[it.id] = offset
			}
		}
	}

	private fun addToMemoryOffsets(id: String, actives: TreeMap<Int, IdNode>): Int {
		val lastEntry = actives.lastEntry() // on top of last item.

		val offset = actives.run {
			if(isEmpty()) 0 else lastEntry.key + lastEntry.value.type.vmCodeSize()
		}

		memoryOffsets[id] = offset
		return offset
	}

	/**
	 * How this algorithm works:
	 * Traverse backwards. If a new variable is found, set it to access the current stack top.
	 * If on the way back you encounter an allocation of this variable, remove it.
	 */
	private fun initializeMemoryOffsets() {
		val actives = TreeMap<Int, IdNode>()

		linearizedCode.reversed().forEach { stmt ->
			when (stmt) {
				is VmInstruction -> {
					stmt.args.filterIsInstance<IdNode>().forEach { arg ->
						if (!memoryOffsets.containsKey(arg.id)) {
							val offset = addToMemoryOffsets(arg.id, actives)
							actives[offset] = arg
						}
					}
				}
				is Alloc -> {
					// remove from actives.
					if (!memoryOffsets.containsKey(stmt.id)) {
						addToMemoryOffsets(stmt.id, actives)
					}
					val varName = stmt.id
					val removedEntry = actives.remove(memoryOffsets[varName]!!)
					require(removedEntry == null || varName == removedEntry.id)
				}
				is VarBound -> {
					stmt.vars.forEach {
						if (!memoryOffsets.containsKey(it.id)) {
							val offset = addToMemoryOffsets(it.id, actives)
							actives[offset] = it
						}
					}
				}
			}
		}
	}

	/**
	 * Adds fn call
	 */
	fun add(op: BaseOp, index: Int) {
		val opIndex = instructionOffsets[op] ?: error("missing $op")
		add(opIndex + index)
	}

	companion object {
		fun createInstructionOffsets(instructions: Collection<VmBaseOp>): Map<VmBaseOp, Int> {
			val retMap = LinkedHashMap<VmBaseOp, Int>()

			instructions.fold(0) { offset, op ->
				run {
					retMap[op] = offset
					offset + op.countArgKinds
				}
			}

			return retMap
		}
	}
}
