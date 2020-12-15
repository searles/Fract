package at.searles.fractlang.nodes

import at.searles.fractlang.Visitor
import at.searles.fractlang.vm.VmArg
import at.searles.fractlang.vm.VmCodeAssembler
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class IdNode(trace: Trace, val id: String): Node(trace), VmArg {
    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun vmCodeSize(): Int {
        return 1
    }

    override fun addToVmCode(vmCodeAssembler: VmCodeAssembler) {
        vmCodeAssembler.addVar(id)
    }

    override fun toString(): String {
        return id
    }

    object Creator: Mapping<String, Node> {
        override fun reduce(input: String, stream: ParserStream): Node {
            return IdNode(stream.createTrace(), input)
        }

        override fun left(result: Node): String? {
            // also covers ops that were already converted.
            return (result as? IdNode)?.id
                ?: (result as? OpNode)?.op?.toString()
        }

        override fun toString(): String {
            return "{id}"
        }
    }

}