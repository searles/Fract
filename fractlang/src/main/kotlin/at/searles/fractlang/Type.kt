package at.searles.fractlang

import at.searles.fractlang.nodes.Node

interface Type {
    fun canConvert(node: Node): Boolean
    fun convert(node: Node): Node
    fun vmCodeSize(): Int // 0 for Bool and Unit
    fun commonType(type: Type): Type?
}