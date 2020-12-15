package at.searles.fractlang.ops

import at.searles.fractlang.BaseTypes
import at.searles.fractlang.nodes.IntNode
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

object Mod: HasSpecialSyntax, StandardOp(3,
    Signature(BaseTypes.Int, BaseTypes.Int, BaseTypes.Int)
) {
    override fun evaluate(trace: Trace, args: List<Node>): Node {
        if(args[0] is IntNode && args[1] is IntNode) {
            return IntNode(trace, imod(trace, (args[0] as IntNode).value, (args[1] as IntNode).value))
        }

        return createApp(trace, args)
    }


    /*
     * Equation to satisfy: x mod y = fract(x / y) * y = (x/y - floor(x/y)) * y
     * 2 mod 3 = 2
     * -2 mod 3 = 1
     * 2 mod -3 = -1
     * -2 mod -3 = -2
     */

    private fun imod(trace: Trace, a: Int, b: Int): Int {
        if(b == 0) {
            throw SemanticAnalysisException("Div/0", trace)
        }

        if(a >= 0 && b > 0) {
            return a % b
        }

        if(a > 0) {
            return -((-b - a % (-b)) % (-b))
        }

        if(b > 0) {
            return (b - (-a) % b) % b
        }

        return -((-a) % (-b))
    }


}