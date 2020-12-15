package at.searles.fractlang.semanticanalysis

import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.SymbolTable
import at.searles.fractlang.nodes.ExternDecl
import at.searles.fractlang.nodes.Node
import at.searles.fractlang.nodes.StringNode
import at.searles.parsing.Trace

/**
 * When parsing externs, this one is used to allow implicit extern definitions.
 */
class AllowImplicitExternsFacade(private val owner: String, private val ownerTrace: Trace, private val analyzer: SemanticAnalysisVisitor) : SymbolTable {
    override fun get(trace: Trace, id: String): Node? {
        if(id == owner) {
            throw SemanticAnalysisException("Self-reference in extern value", ownerTrace)
        }

        val node = analyzer.table[ownerTrace, id]

        if(node == null) {
            ExternDecl(ownerTrace, id, StringNode(ownerTrace, "$id (defined in \"$owner\")"), defaultExpr).accept(analyzer)
            return analyzer.table[ownerTrace, id] ?: error("undefined implicit extern")
        }

        return node
    }

    override fun addExternValue(trace: Trace, name: String, description: String, expr: String) {
        analyzer.table.addExternValue(ownerTrace, name, description, expr)
    }

    override fun setScale(trace: Trace, scale: Scale) {
        throw SemanticAnalysisException("setScale not allowed in extern", trace)
    }

    override fun addPalette(trace: Trace, description: String, defaultPalette: Palette): Int {
        throw SemanticAnalysisException("addPalette not allowed in extern", trace)
    }

    override fun putPalette(trace: Trace, label: String, description: String, defaultPalette: Palette): Int {
        throw SemanticAnalysisException("putPalette not allowed in extern", trace)
    }

    companion object {
        const val defaultExpr = "0"
    }
}
