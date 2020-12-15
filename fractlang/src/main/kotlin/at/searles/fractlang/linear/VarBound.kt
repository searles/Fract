package at.searles.fractlang.linear

import at.searles.fractlang.nodes.IdNode

/**
 * After loops, all variables that were used before the loop should
 * already exist to avoid that they are marked as unused in the loop.
 */
class VarBound(val vars: List<IdNode>): CodeLine {
    override fun toString(): String {
        return "VarBound $vars"
    }
}