package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Type
import at.searles.fractlang.Visitor
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Fold
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace
import java.lang.IllegalArgumentException

class VarDecl(trace: Trace, val name: String, private val varTypeString: String?, val init: Node?): Node(trace) {

    init {
        type = BaseTypes.Unit
    }

    val varType: Type? by lazy {
        try {
            varTypeString?.run {
                BaseTypes.values().find { this == it.toString() }
            }
        } catch (e: IllegalArgumentException) {
            throw SemanticAnalysisException("no such type: $varTypeString", trace)
        }
    }

    override fun <T> accept(visitor: Visitor<T>): T {
        return visitor.visit(this)
    }

    override fun toString(): String {
        return "var $name: $varType = $init"
    }

    object CreatorWithoutInit: Mapping<Node, Node> {
        override fun reduce(input: Node, stream: ParserStream): Node {
            return (input as VarParameter).let {
                VarDecl(stream.createTrace(), it.name, it.varTypeString, null)
            }
        }

        override fun left(result: Node): Node? {
            return (result as? VarDecl)?.let {
                if(it.init == null) {
                    VarParameter(it.trace, it.name, it.varTypeString)
                } else {
                    null
                }
            }
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }

    object CreatorWithInit: Fold<Node, Node, Node> {
        override fun apply(stream: ParserStream, left: Node, right: Node): Node {
            return (left as VarParameter).let {
                VarDecl(stream.createTrace(), it.name, it.varTypeString, right)
            }
        }

        override fun leftInverse(result: Node): Node? {
            return (result as? VarDecl)?.let {
                if(it.init != null) {
                    VarParameter(it.trace, it.name, it.varTypeString)
                } else {
                    null
                }
            }
        }

        override fun rightInverse(result: Node): Node? {
            return (result as? VarDecl)?.init
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }
}