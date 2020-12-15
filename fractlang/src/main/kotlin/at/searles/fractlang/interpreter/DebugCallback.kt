package at.searles.fractlang.interpreter

import at.searles.fractlang.nodes.*

interface DebugCallback {
    fun step(interpreter: Interpreter, node: Node)
}