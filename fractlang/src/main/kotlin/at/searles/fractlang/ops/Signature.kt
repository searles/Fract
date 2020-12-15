package at.searles.fractlang.ops

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.Type

class Signature(val returnType: Type, vararg val argTypes: Type) {
    fun convertArguments(args: List<Node>): List<Node> {
        require(args.size >= argTypes.size)
        return argTypes.zip(args).map { it.first.convert(it.second) }
    }

    fun matches(args: List<Node>): Boolean {
        return argTypes.size <= args.size && argTypes.zip(args).all { it.first.canConvert(it.second) }
    }

    fun matchesExact(args: List<Node>): Boolean {
        return argTypes.size == args.size && argTypes.zip(args).all {
            it.first == it.second.type
        }
    }
}