package at.searles.fractlang.nodes

interface NumValue: ConstValue {
    fun isZero(): Boolean
    fun isOne(): Boolean
}