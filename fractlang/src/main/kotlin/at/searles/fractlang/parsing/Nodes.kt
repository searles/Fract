package at.searles.fractlang.parsing

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.Type
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.Op
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.*
import at.searles.regexparser.CodePointStream
import at.searles.regexparser.EscStringParser

val toInt = {s: CharSequence -> s.toString().toBigInteger()}
val toHex = {s: CharSequence -> s.substring(1).toBigInteger(16).toInt().toBigInteger()} // this must cover #ffffffff
val toReal = {s: CharSequence -> s.toString().toDouble()}
val toIdString = {s: CharSequence -> s.toString()}

object EscStringMapper: Mapping<CharSequence, String> {
    override fun reduce(left: CharSequence, stream: ParserStream): String {
        // TODO How to handle exception?
        return EscStringParser.parse(CodePointStream(left.toString()))
    }

    override fun left(result: String): CharSequence? {
        return EscStringParser.unparse(result)
    }

    override fun toString(): String {
        return "{escString}"
    }
}

private fun appArgOrNull(app: Node, op: Op, arity: Int, index: Int): Node? {
    if(app !is App
        || app.head !is OpNode
        || app.head.op != op
        || app.args.size != arity) return null

    return app.args[index]
}

fun unaryCreator(op: Op): Mapping<Node, Node> {
    return object: Mapping<Node, Node> {
        override fun reduce(left: Node, stream: ParserStream): Node {
            return App(stream.createTrace(), op, listOf(left))
        }

        override fun left(result: Node): Node? {
            return appArgOrNull(result, op, 1, 0)
        }

        override fun toString(): String {
            return "{${op.javaClass.simpleName}}"
        }
    }
}


fun BinaryCreator(op: Op): Fold<Node, Node, Node> {
    return object: Fold<Node, Node, Node> {
        override fun apply(stream: ParserStream, left: Node, right: Node): Node {
            return App(stream.createTrace(), op, listOf(left, right))
        }

        override fun leftInverse(result: Node): Node? {
            return appArgOrNull(result, op, 2, 0)
        }

        override fun rightInverse(result: Node): Node? {
            return appArgOrNull(result, op, 2, 1)
        }

        override fun toString(): String {
            return "{${op.javaClass.simpleName}}"
        }
    }
}

fun stringToType(trace: Trace, typeName: String): Type {
    return BaseTypes.values().firstOrNull { it.name == typeName }
        ?: throw SemanticAnalysisException("no such type", trace)
}

object ToType: Mapping<String, Type> {
    override fun reduce(left: String, stream: ParserStream): Type {
        return stringToType(stream.createTrace(), left)
    }

    override fun left(result: Type): String {
        return result.toString()
    }

    override fun toString(): String {
        return "{type}"
    }
}

fun toBool(value: Boolean): Initializer<Boolean> {
    return object: Initializer<Boolean> {
        override fun parse(stream: ParserStream): Boolean {
            return value
        }

        override fun consume(t: Boolean): Boolean {
            return t == value
        }

        override fun toString(): String {
            return "{${value}}"
        }
    }
}


/**
 * Identity in parse-direction. In left-direction, this
 * mapping passes blocks through so that no semicolons
 * are added after blocks.
 */
object SkipSemicolon: Mapping<Node, Node> {
    override fun reduce(left: Node, stream: ParserStream): Node {
        return left
    }

    override fun left(result: Node): Node? {
        if(result is Block || result is ClassDecl || (result is FunDecl && result.body is Block)) {
            return result
        }

        return null
    }

    override fun toString(): String {
        return "{;}"
    }
}

