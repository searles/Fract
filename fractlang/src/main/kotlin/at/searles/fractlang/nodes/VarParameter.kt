package at.searles.fractlang.nodes

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Type
import at.searles.fractlang.Visitor
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Fold
import at.searles.parsing.Mapping
import at.searles.parsing.ParserStream
import at.searles.parsing.Trace

class VarParameter(trace: Trace, val name: String, val varTypeString: String?): Node(trace) {

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
        return "var $name: $varType"
    }

    object CreatorWithType: Fold<String, String, Node> {
        override fun apply(stream: ParserStream, left: String, right: String): Node {
            return VarParameter(stream.createTrace(), left, right)
        }

        override fun leftInverse(result: Node): String? {
            return (result as? VarParameter)?.let {
                if(it.varType != null) it.name else null
            }
        }

        override fun rightInverse(result: Node): String? {
            return (result as? VarParameter)?.varTypeString
        }
    }

    object CreatorWithoutType: Mapping<String, Node> {
        override fun reduce(left: String, stream: ParserStream): Node {
            return VarParameter(stream.createTrace(), left, null)
        }

        override fun left(result: Node): String? {
            return (result as? VarParameter)?.let {
                if(it.varType == null) it.name else null
            }
        }

        override fun toString(): String {
            return javaClass.simpleName
        }
    }
}