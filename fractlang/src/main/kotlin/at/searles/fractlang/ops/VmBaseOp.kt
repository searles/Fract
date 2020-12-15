package at.searles.fractlang.ops

import at.searles.fractlang.nodes.Node

abstract class VmBaseOp(vararg signatures: Signature): BaseOp(*signatures) {
    /**
     * Returns the count of possible argument combinations consisting
     * of Type/IsConst
     */
    abstract val countArgKinds: Int

    /**
     * Returns the concrete parameter configuration for the given offset.
     */
    abstract fun getArgKindAt(offset: Int): List<ArgKind>

    /**
     * Inverse of getArgKindAt
     */
    abstract fun getArgKindOffset(args: List<Node>): Int

    /**
     * Returns the signature that is used for the parameter configuration
     * of the given index.
     */
    abstract fun getSignatureAt(offset: Int): Signature
}