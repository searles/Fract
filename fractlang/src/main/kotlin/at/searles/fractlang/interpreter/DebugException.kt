package at.searles.fractlang.interpreter

import at.searles.parsing.Trace

class DebugException(message: String, val trace: Trace) : RuntimeException(message)
