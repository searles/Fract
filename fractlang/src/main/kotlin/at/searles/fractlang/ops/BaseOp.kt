package at.searles.fractlang.ops

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.Type
import at.searles.fractlang.nodes.App
import at.searles.parsing.Trace

/**
 * These operations are directly executed in the Vm.
 */
abstract class BaseOp(vararg val signatures: Signature) : Op {
    init {
        require(signatures.all { it.argTypes.size == signatures[0].argTypes.size })
    }

    val argsCount = signatures[0].argTypes.size

    override fun apply(trace: Trace, args: List<Node>): Node {
        val signature = signatures.find { it.matches(args) } ?:
            throw SemanticAnalysisException(
                "no matching signature in $this for $args",
                trace
            )

        val typedArgs = signature.convertArguments(args)

        return evaluate(trace, typedArgs)
    }

    fun createApp(trace: Trace, vararg args: Node): Node {
        return createApp(trace, listOf(*args))
    }

    fun createApp(trace: Trace, args: List<Node>): Node {
        val returnType = signatures.find { it.matches(args) }?.returnType

        require(returnType != null) { "optimizer returned weird arguments for $this: $args" }

        return App(trace, this, args).apply { type = returnType }
    }

    /**
     * Creates a node representing an application of this to args.
     * This one must be properly typed.
     */
    abstract fun evaluate(trace: Trace, args: List<Node>): Node


    override fun toString(): String {
        return javaClass.simpleName
    }

    data class ArgKind(val type: Type, val isConst: Boolean)
}
