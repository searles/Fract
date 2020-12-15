package at.searles.fractlang.ops

import at.searles.fractlang.nodes.*
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

/**
 * diff(formula, variable)
 */
object Diff: Op {
    override fun apply(trace: Trace, args: List<Node>): Node {
        val x = args[1] as? IdNode ?:
            throw SemanticAnalysisException("must be a variable", trace)

        return diff(trace, args[0], x)
    }

    private fun diff(trace: Trace, f: Node, x: IdNode): Node {
        return when(f) {
            is App -> diffApp(trace, f.head, f.args, x)
            is IdNode -> IntNode(trace, if(f.id == x.id) 1 else 0)
            is NumValue -> IntNode(trace, 0)
            else -> throw SemanticAnalysisException("not differentiable", f.trace)
        }
    }

    private fun diffApp(trace: Trace, head: Node, args: List<Node>, x: IdNode): Node {
        // App-head is
        //  (x+1), ie App --> Multiplication.
        //      x, ie Id that is a value --> Multiplication
        //      2, ie a number
        //    sin, ie an Instruction

        if(head is OpNode) {
            return diff(trace, head.op, args, x)
        }

        if(args.size == 1) {
            return diff(trace, Mul, listOf(head, args[0]), x)
        }

        throw SemanticAnalysisException("not differentiable", head.trace)
    }

    private fun diff(trace: Trace, op: MetaOp, args: List<Node>, x: IdNode): Node {
        // differential of all arguments
        val dArgs = args.map { apply(it.trace, listOf(it, x)) }

        return when (args.size) {
            2 -> diffBinary(trace, op, args, dArgs)
            1 -> diffUnary(trace, op, args[0], dArgs[0])
            else -> throw SemanticAnalysisException("No derivative of $op", trace)

        }
    }

    private fun diffBinary(trace: Trace, op: MetaOp, args: List<Node>, dArgs: List<Node>): Node {
        return when (op) {
            is Add -> Add.apply(trace, dArgs[0], dArgs[1])
            is Sub -> Sub.apply(trace, dArgs[0], dArgs[1])
            is Mul -> Add.apply(trace,
                Mul.apply(trace, dArgs[0], args[1]),
                Mul.apply(trace, dArgs[1], args[0])
            )
            is Div -> Div.apply(trace,
                Sub.apply(trace,
                    Mul.apply(trace, dArgs[0], args[1]),
                    Mul.apply(trace, args[0], dArgs[1])
                ),
                Pow.apply(trace, args[1], IntNode(trace, 2))
            )
            is Pow -> Mul.apply(trace,
                Pow.apply(trace, args[0], args[1]),
                Add.apply(trace,
                    Div.apply(trace,
                        Mul.apply(trace,
                            args[1],
                            dArgs[0]
                        ),
                        args[0]
                    ),
                    Mul.apply(trace, Log.apply(trace, args[0]), dArgs[1])
                )
            )
            else -> throw SemanticAnalysisException("No derivative of $op.", trace)
        }
    }

    private fun diffUnary(trace: Trace, op: MetaOp, arg: Node, dArg: Node): Node {
        return when (op) {
            is Neg -> Neg.apply(trace, dArg)
            is Recip -> Neg.apply(trace, Div.apply(trace,
                dArg,
                Pow.apply(trace, arg, IntNode(trace, 2))
            ))
            is Sqrt ->
                Div.apply(trace,
                    dArg,
                    Sqrt.apply(trace, arg)
                )
            is Log -> Div.apply(trace, dArg, arg)
            is Exp -> Mul.apply(trace, Exp.apply(trace, arg), dArg)
            is Sin -> Mul.apply(trace, Cos.apply(trace, arg), dArg)
            is Cos -> Neg.apply(trace, Mul.apply(trace, Sin.apply(trace, arg), dArg))
            is Tan -> Div.apply(trace, dArg, Sqr.apply(trace, Cos.apply(trace, arg)))
            is Asin -> Div.apply(trace, dArg, Sqrt.apply(trace, Sub.apply(trace, IntNode(trace, 1), Sqr.apply(trace, arg))))
            is Acos -> Neg.apply(trace, Div.apply(trace, dArg, Sqrt.apply(trace, Sub.apply(trace, IntNode(trace, 1), Sqr.apply(trace, arg)))))
            is Atan -> Div.apply(trace, dArg, Add.apply(trace, IntNode(trace, 1), Sqr.apply(trace, arg)))
            is Sinh -> Mul.apply(trace, Cosh.apply(trace, arg), dArg)
            is Cosh -> Mul.apply(trace, Sinh.apply(trace, arg), dArg)
            is Tanh -> Div.apply(trace, dArg, Sqr.apply(trace, Cosh.apply(trace, arg)))
            is Asinh -> Div.apply(trace, dArg, Sqrt.apply(trace, Add.apply(trace, Sqr.apply(trace, arg), IntNode(trace, 1))))
            is Acosh -> Div.apply(trace, dArg, Sqrt.apply(trace, Sub.apply(trace, Sqr.apply(trace, arg), IntNode(trace, 1))))
            is Atanh -> Div.apply(trace, dArg, Sub.apply(trace, IntNode(trace, 1), Sqr.apply(trace, arg)))
            else -> throw SemanticAnalysisException("No derivative of $op.", trace)
        }
    }
}