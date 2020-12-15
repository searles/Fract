package at.searles.fractlang.ops

import at.searles.fractlang.nodes.ConstValue
import at.searles.fractlang.nodes.Node

/**
 * Order of configuration is
 *    * reg, reg, ..., reg.
 *    * val, reg, ..., reg
 *    * reg, val, ..., reg
 *    ...
 *    * val, val ...., val (like binary)
 */
abstract class StandardOp(private val countConfigPerSignature: Int, vararg signatures: Signature): VmBaseOp(*signatures) {
    override val countArgKinds: Int = countConfigPerSignature * signatures.size

    override fun getArgKindAt(offset: Int): List<ArgKind> {
        val signature = getSignatureAt(offset)
        val index = offset % countConfigPerSignature

        require(index < countConfigPerSignature)

        val argKinds = ArrayList<ArgKind>(argsCount)

        for(i in 0 until argsCount) {
            val isConst = ((1 shl i) and index != 0)
            argKinds.add(ArgKind(signature.argTypes[i], isConst))
        }

        return argKinds
    }

    override fun getArgKindOffset(args: List<Node>): Int {
        var index = 0

        for(i in 0 until argsCount) {
            if(args[i] is ConstValue) {
                index = index or (1 shl i)
            }
        }

        return index + countConfigPerSignature * signatures.indices.find { signatures[it].matchesExact(args) }!!
    }

    override fun getSignatureAt(offset: Int): Signature {
        return signatures[offset / countConfigPerSignature]
    }
}