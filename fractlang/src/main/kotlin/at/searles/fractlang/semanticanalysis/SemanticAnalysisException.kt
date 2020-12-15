package at.searles.fractlang.semanticanalysis

import at.searles.parsing.Trace
import java.lang.RuntimeException

class SemanticAnalysisException(message: String, val trace: Trace) : RuntimeException(message)
