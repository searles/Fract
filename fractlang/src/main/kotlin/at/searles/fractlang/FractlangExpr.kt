package at.searles.fractlang

import at.searles.fractlang.nodes.Node
import at.searles.fractlang.parsing.FractlangGrammar
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.fractlang.semanticanalysis.SemanticAnalysisVisitor
import at.searles.parsing.ParserStream

object FractlangExpr {
    fun fromString(expr: String): Node {
        val sourceCodeStream = ParserStream.create(expr)

        val ast = FractlangGrammar.program.parse(sourceCodeStream)
            ?: throw SemanticAnalysisException("Could not parse program", sourceCodeStream.createTrace())

        if(!FractlangGrammar.eof.recognize(sourceCodeStream)) {
            throw SemanticAnalysisException("Expression not fully parsed", sourceCodeStream.createTrace())
        }

        val symbolTable = RootSymbolTable(FractlangProgram.namedInstructions, emptyMap())
        val varNameGenerator = NameGenerator()

        return ast.accept(SemanticAnalysisVisitor(symbolTable, varNameGenerator))
    }
}