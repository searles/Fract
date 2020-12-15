package at.searles.fractlang.semanticanalysis

import at.searles.commons.math.Cplx
import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.*
import at.searles.parsing.Trace
import kotlin.math.max
import kotlin.math.pow

object Optimizer {

	fun isAllNum(nodes: List<Node>): Boolean {
		return nodes.all {it is NumValue }
	}

	private fun isOp(node: Node, op: BaseOp): Boolean {
		return node is App && node.head is OpNode && node.head.op == op
	}

	private fun isZero(arg: Node): Boolean {
		return arg is NumValue && arg.isZero()
	}

	private fun isOne(arg: Node): Boolean {
		return arg is NumValue && arg.isOne()
	}

	fun add(trace: Trace, args: List<Node>): Node {
		// N + N -> N
		if(isAllNum(args)) {
			return when(val arg0 = args[0]) {
				is IntNode -> IntNode(trace, arg0.value + (args[1] as IntNode).value)
				is RealNode -> RealNode(trace, arg0.value + (args[1] as RealNode).value)
				is CplxNode -> CplxNode(trace, Cplx().add(arg0.value, (args[1] as CplxNode).value))
				else -> error("something went wrong with the cast")
			}
		}

		// x + N -> N + x
		if(args[1] is ConstValue) return add(
            trace,
            listOf(args[1], args[0])
        )
		
		// 0 + x -> x
		if(isZero(args[0])) return args[1]

		// x + (y + z) -> (x + y) + z
		if(isOp(args[1], Add)) return Add.apply(trace, listOf(
            add(
                trace,
                listOf(args[0], (args[1] as App).args[0])
            ),
				(args[1] as App).args[1]))

		// x + (y - z) -> (x + y) - z
		if(isOp(
                args[1],
                Sub
            )
        ) return sub(
            trace, listOf(
                add(
                    trace,
                    listOf(args[0], (args[1] as App).args[0])
                ),
                (args[1] as App).args[1]
            )
        )

		// x + -y -> x - y
		if(isOp(
                args[1],
                Neg
            )
        ) return sub(
            trace,
            listOf(args[0], (args[1] as App).args[0])
        )

		// -x + y -> y - x
		if(isOp(
                args[0],
                Neg
            )
        ) return sub(
            trace,
            listOf(args[1], (args[0] as App).args[0])
        )
		
		return Add.createApp(trace, args)
	}

	fun sub(trace: Trace, args: List<Node>): Node {
		// N - N -> N
		if(isAllNum(args)) {
			return when(val arg0 = args[0]) {
				is IntNode -> IntNode(trace, arg0.value - (args[1] as IntNode).value)
				is RealNode -> RealNode(trace, arg0.value - (args[1] as RealNode).value)
				is CplxNode -> CplxNode(trace, Cplx().sub(arg0.value, (args[1] as CplxNode).value))
				else -> error("something went wrong with the cast")
			}
		}

		// x - N -> -N + x
		if(args[1] is ConstValue) return add(
            trace,
            listOf(neg(trace, listOf(args[1])), args[0])
        )
		
		// 0 - x -> -x
		if(isZero(args[0])) return neg(
            trace,
            listOf(args[1])
        )

		// x - (y + z) -> (x - y) - z
		if(isOp(
                args[1],
                Add
            )
        ) return sub(
            trace, listOf(
                sub(
                    trace,
                    listOf(args[0], (args[1] as App).args[0])
                ),
                (args[1] as App).args[1]
            )
        )

		// x - (y - z) -> (x - y) + z
		if(isOp(
                args[1],
                Sub
            )
        ) return add(
            trace, listOf(
                sub(
                    trace,
                    listOf(args[0], (args[1] as App).args[0])
                ),
                (args[1] as App).args[1]
            )
        )

		// -x - y -> -(x + y)
		if(isOp(
                args[0],
                Neg
            )
        ) return neg(
            trace,
            listOf(
                add(
                    trace,
                    listOf((args[0] as App).args[0], args[1])
                )
            )
        )
		
		// x - -y -> x + y
		if(isOp(
                args[1],
                Neg
            )
        ) return add(
            trace,
            listOf(args[0], (args[1] as App).args[0])
        )
		
		return Sub.createApp(trace, args)
	}

	fun neg(trace: Trace, args: List<Node>): Node {
		// -N -> N
		if(isAllNum(args)) {
			return when(val arg0 = args[0]) {
				is IntNode -> IntNode(trace, -arg0.value)
				is RealNode -> RealNode(trace, -arg0.value)
				is CplxNode -> CplxNode(trace, Cplx().neg(arg0.value))
				else -> error("something went wrong with the cast")
			}
		}

		// --x -> 
		if(isOp(args[0], Neg)) return (args[0] as App).args[0]

		// -(x - y) -> y - x
		if(isOp(
                args[0],
                Sub
            )
        ) return sub(
            trace,
            listOf((args[0] as App).args[1], (args[0] as App).args[0])
        )

		return Neg.createApp(trace, args)
	}

    fun abs(trace: Trace, args: List<Node>): Node {
		return when(val arg = args[0]) {
			is IntNode -> IntNode(trace, kotlin.math.abs(arg.value))
			is RealNode -> RealNode(trace, kotlin.math.abs(arg.value))
			is CplxNode -> RealNode(trace, arg.value.rad())
			else -> Abs.createApp(trace, args)
		}
    }

	fun mul(trace: Trace, args: List<Node>): Node {
		// N * N -> N
		if(isAllNum(args)) {
			return when(val arg0 = args[0]) {
				is IntNode -> IntNode(trace, arg0.value * (args[1] as IntNode).value)
				is RealNode -> RealNode(trace, arg0.value * (args[1] as RealNode).value)
				is CplxNode -> CplxNode(trace, Cplx().mul(arg0.value, (args[1] as CplxNode).value))
				else -> error("something went wrong with the cast")
			}
		}

		// x * N -> N * x
		if(args[1] is NumValue) return mul(
            trace,
            listOf(args[1], args[0])
        )

		// 0 * x -> 0
		if(isZero(args[0])) return args[0]

		// 1 * x -> 1
		if(isOne(args[0])) return args[1]

		// -x * y -> -(x * y)
		if(isOp(
                args[0],
                Neg
            )
        ) return neg(
            trace,
            listOf(
                mul(
                    trace,
                    listOf((args[0] as App).args[0], args[1])
                )
            )
        )

		// x * -y -> -(x * y)
		if(isOp(
                args[1],
                Neg
            )
        ) return neg(
            trace,
            listOf(
                mul(
                    trace,
                    listOf(args[0], (args[1] as App).args[0])
                )
            )
        )

        // /x * y -> y / x
        if(isOp(args[0], Recip)) {
            return Div.apply(trace, args[1], (args[0] as App).args[0])
        }

        // x * /y -> x / y
        if(isOp(args[1], Recip)) {
            return Div.apply(trace, args[0], (args[1] as App).args[0])
        }

        return Mul.createApp(trace, args)
	}

	fun div(trace: Trace, args: List<Node>): Node {
		// N / N -> N
		if(isAllNum(args)) {
			return when(val arg0 = args[0]) {
				is IntNode -> IntNode(trace, arg0.value / (args[1] as IntNode).value)
				is RealNode -> RealNode(trace, arg0.value / (args[1] as RealNode).value)
				is CplxNode -> CplxNode(trace, Cplx().div(arg0.value, (args[1] as CplxNode).value))
				else -> error("something went wrong with the cast")
			}
		}

		// x / N -> /N * x
		if(args[1] is NumValue) return mul(
            trace,
            listOf(
                recip(trace, listOf(args[1])),
                args[0]
            )
        )

		// 0 * x -> 0
		if(isZero(args[0])) return args[0]

		// 1 / x -> 1
		if(isOne(args[0])) return recip(
            trace,
            listOf(args[1])
        )

		// -x / y -> -(x / y)
		if(isOp(
                args[0],
                Neg
            )
        ) return neg(
            trace,
            listOf(
                div(
                    trace,
                    listOf((args[0] as App).args[0], args[1])
                )
            )
        )

		// x / -y -> -(x / y)
		if(isOp(
                args[1],
                Neg
            )
        ) return neg(
            trace,
            listOf(
                div(
                    trace,
                    listOf(args[0], (args[1] as App).args[0])
                )
            )
        )

		return Div.createApp(trace, args)
	}

	fun recip(trace: Trace, args: List<Node>): Node {
		// -N -> N
		if(isAllNum(args)) {
			return when(val arg = args[0]) {
				is RealNode -> RealNode(trace, 1.0 / arg.value)
				is CplxNode -> CplxNode(trace, Cplx().rec(arg.value))
				else -> error("something went wrong with the cast")
			}
		}

		// //x ->
		if(isOp(args[0], Recip)) return (args[0] as App).args[0]

		// /(x / y) -> y / x
		if(isOp(
                args[0],
                Div
            )
        ) return div(
            trace,
            listOf((args[0] as App).args[1], (args[0] as App).args[0])
        )

		return Recip.createApp(trace, args)
	}

    fun re(trace: Trace, args: List<Node>): Node {
        if(args[0] is CplxNode) {
            return RealNode(trace, (args[0] as CplxNode).value.re())
        }

        if(isOp(args[0], Cons)) {
            val re = (args[0] as App).args[0]
            val im = (args[0] as App).args[1]

            if (isZero(im)) {
                return re
            }
        }

        return RealPart.createApp(trace, args)
    }

    fun im(trace: Trace, args: List<Node>): Node {
        if(args[0] is CplxNode) {
            return RealNode(trace, (args[0] as CplxNode).value.im())
        }

        if(isOp(args[0], Cons)) {
            val re = (args[0] as App).args[0]
            val im = (args[0] as App).args[1]

            if (isZero(re)) {
                return im
            }
        }

        return ImagPart.createApp(trace, args)
    }

    fun pow(trace: Trace, args: List<Node>): Node {
        if(isAllNum(args)) {
            return when(val arg0 = args[0]) {
                is RealNode ->
                    when(val arg1 = args[1]) {
                        is IntNode -> RealNode(trace, arg0.value.pow(arg1.value))
                        is RealNode -> RealNode(trace, arg0.value.pow(arg1.value))
                        else -> throw IllegalArgumentException()
                    }
                is CplxNode ->
                    when(val arg1 = args[1]) {
                        is IntNode -> CplxNode(trace, Cplx().powInt(arg0.value, arg1.value))
                        is RealNode -> CplxNode(trace, Cplx().pow(arg0.value, Cplx(arg1.value)))
                        is CplxNode -> CplxNode(trace, Cplx().pow(arg0.value, arg1.value))
                        else -> throw IllegalArgumentException()
                    }
                else -> error("bug")
            }
        }

        if(isZero(args[1])) {
            return when(args[0].type) {
                BaseTypes.Real -> RealNode(trace, 1.0)
                BaseTypes.Cplx -> CplxNode(trace, Cplx(1.0))
                else -> error("bug")
            }
        }

        if(isOne(args[1])) {
            return args[0]
        }

        if(isZero(args[0]) || isOne(args[1])) {
            return args[0]
        }

        if(isOp(args[0], Pow)) {
            val base = (args[0] as App).args[0]
            val exp0 = (args[0] as App).args[1]
            val exp1 = args[1]

            return pow(trace, listOf(base, Mul.apply(trace, exp0, exp1)))
        }

        return Pow.createApp(trace, args)
    }
}
