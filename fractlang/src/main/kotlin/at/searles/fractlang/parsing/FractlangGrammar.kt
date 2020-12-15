package at.searles.fractlang.parsing

import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.*
import at.searles.lexer.Lexer
import at.searles.lexer.SkipTokenizer
import at.searles.lexer.Tokenizer
import at.searles.parsing.*
import at.searles.parsing.Reducer.Companion.opt
import at.searles.parsing.Reducer.Companion.rep
import at.searles.parsing.ref.RefParser
import at.searles.parsing.tokens.TokenParser
import at.searles.parsing.tokens.TokenRecognizer
import at.searles.parsingtools.common.PairCreator
import at.searles.parsingtools.common.Init
import at.searles.parsingtools.list.EmptyListCreator
import at.searles.parsingtools.list.ListCreator
import at.searles.regexp.CharSet
import at.searles.regexp.Text
import at.searles.regexparser.RegexpParser

open class Grammar<T: Tokenizer>(val tokenizer: T) {

    val eof by lazy {
        val regexp = CharSet.eof()
        val tokenId = tokenizer.add(regexp)
        TokenRecognizer(tokenId, tokenizer, "")
    }

    fun regexOf(regexStr: String): TokenParser {
        val regex = RegexpParser.parse(regexStr)
        val tokenId = tokenizer.add(regex)

        return TokenParser(tokenId, tokenizer)
    }

    fun <T> regexOf(regexStr: String, conversion: (CharSequence) -> T): Parser<T> {
        val mapping = Mapping.create<CharSequence, T>( { v -> v.toString() } ) { s -> conversion(s) }
        return regexOf(regexStr) + mapping
    }

    fun keywordOf(text: String): TokenRecognizer {
        val regex = Text(text)
        val tokenId = tokenizer.add(regex)
        return TokenRecognizer(tokenId, tokenizer, text)
    }

    operator fun <T, U> Reducer<T, U>.plus(right: String): Reducer<T, U> {
        return this + keywordOf(right)
    }

    operator fun <T> Parser<T>.plus(right: String): Parser<T> {
        return this + keywordOf(right)
    }

    operator fun Recognizer.plus(right: String): Recognizer {
        return this + keywordOf(right)
    }

    fun String.ref(label: String): Recognizer {
        return keywordOf(this).ref(label)
    }
}

object FractlangGrammar: Grammar<SkipTokenizer>(SkipTokenizer(Lexer())) {

    private val whiteSpaceRex = RegexpParser.parse("""[\t\x0b\r\n\x0c \x85\xa0\u1680\u2000-\u200a\u2028\u2029\u202f\u205f\u3000]+""")
    private val multilineCommentRex = RegexpParser.parse("""('/*' .* '*/')!""")
    private val singlelineCommentRex = RegexpParser.parse("""'//' [^\n]*""")

    val whiteSpaceId: Int
    val multilineCommentId: Int
    val singlelineCommentId: Int

    init {
        whiteSpaceId = tokenizer.addSkipped(whiteSpaceRex)
        multilineCommentId = tokenizer.addSkipped(singlelineCommentRex)
        singlelineCommentId = tokenizer.addSkipped(multilineCommentRex)
    }

    val intRex = "[0-9]+"
    val hexRex = "'#' [0-9A-Fa-f]{1,8}"
    val decimals = "'.'[0-9]*"
    val exponent = "[eE]'-'?[0-9]+"
    val realRex = "[0-9]+ ($decimals | $exponent | $decimals $exponent)"
    val identifierRex = "[a-zA-Z_][a-zA-Z0-9_]* - ('else' | 'if' | 'while' | 'val' | 'var' | 'fun' | 'for' | 'in' | 'class' | 'extern' | 'and' | 'or' | 'xor' | 'true' | 'false')"
    val stringRex = """('"' ([^\"] | '\\'. )* '"')"""

    val intNum = regexOf(intRex) { toInt(it) }
    val realNum = regexOf(realRex) { toReal(it) }
    val hexNum = regexOf(hexRex) { toHex(it) }
    val identifier = regexOf(identifierRex) { toIdString(it) }

    val str = regexOf(stringRex) + EscStringMapper

    val bool =
            keywordOf("true") + Init(true) or
            keywordOf("false") + Init(false)

    val intNode = (intNum or hexNum) + IntNode.Creator
    val realNode = realNum + RealNode.Creator
    val stringNode = str + StringNode.Creator
    val idNode = identifier + IdNode.Creator
    val boolNode = bool + BoolNode.Creator

    val atom = intNode.ref(Annot.Const) or
            realNode.ref(Annot.Const) or
            stringNode.ref(Annot.Str) or
            boolNode.ref(Annot.Const) or
            idNode.ref(Annot.Name)

    val comma = ",".ref(Annot.Comma)

    val expr = RefParser<Node>("expr")
    val stmt = RefParser<Node>("stmt")
    val stmts = RefParser<List<Node>>("stmts")
    val app = RefParser<Node>("app")

    val exprList = expr.rep(comma)

    val qualifier = keywordOf(".") + identifier + (QualifiedNode.Creator)
    val index = keywordOf("[") + expr + (IndexedNode.Creator) + "]"
    val multiArgument = keywordOf("(") + exprList + AppChain.Creator + ")"
    val singleArgument = (app + ListCreator()) + AppChain.Creator

    val vector = keywordOf("[") + exprList + VectorNode.Creator + "]"

    val appHead = atom or
            vector or
            keywordOf("(") + expr + ")"

    val appHeadAbs = keywordOf("|") + expr + "|" + unaryCreator(Abs)

    // abs is tough: |1 + log |x|| and |1 + x| y cannot be distinguished.
    //
    // log |x| should be fine. |x| y is not fine. |x| * y is fine.
    // Hence, after abs, there is no single-argument allowed.

    val appPrinter = object: Mapping<Node, Node> {
        override fun reduce(left: Node, stream: ParserStream): Node {
            return left
        }

        override fun left(result: Node): Node {
            if(result !is App) {
                return result
            }

            if(result.head is OpNode) {
                when(result.head.op) {
                    Add, Sub, Mul, Div, Pow, Neg, Abs -> return result
                }
            }

            return AppChain(result.trace, result.head, result.args)
        }
    }

    init {
        app.ref = (
            appHeadAbs + (qualifier or index or multiArgument).rep() or
            appHead + (qualifier or index or multiArgument or singleArgument).rep()
        ) + appPrinter
    }

    val ifExpr = "if".ref(Annot.Keyword) +
            "(" + expr + ")" +
            (stmt + If.Creator) + (
                "else".ref(Annot.Keyword) + stmt + IfElse.Creator
            ).opt()

    val block = "{".ref(Annot.Newline) + stmts.ref(Annot.Intent).ref(Annot.Newline) + Block.Creator + "}"

    val term = ifExpr or block or app

    val literal = RefParser<Node>("literal")

    val literalRef = 
            keywordOf("-") + literal + unaryCreator(Neg) or
            keywordOf("/") + literal + unaryCreator(Recip) or
            term

    init { literal.ref = literalRef }

    val cons = literalRef + (keywordOf(":") + literalRef + (BinaryCreator(Cons))).opt()

    val powRef = RefParser<Node>("pow")

    val pow = cons + (keywordOf("^") + powRef + (BinaryCreator(Pow)) ).opt()
    
    init { powRef.ref = pow }

    val product = pow + (
            keywordOf("*") + pow + (BinaryCreator(Mul)) or 
            keywordOf("/") + pow + (BinaryCreator(Div)) or 
            keywordOf("%") + pow + (BinaryCreator(Mod))
    ).rep()

    val sum = product + (
            keywordOf("+") + product + (BinaryCreator(Add)) or 
            keywordOf("-") + product + (BinaryCreator(Sub))
    ).rep()

    val cmp = sum + ( 
            keywordOf(">") +  sum + (BinaryCreator(Greater)) or 
            keywordOf(">=") + sum + (BinaryCreator(GreaterEqual)) or
            keywordOf("<=") + sum + (BinaryCreator(LessEqual)) or
            keywordOf("<") + sum + (BinaryCreator(Less)) or
            keywordOf("==") + sum + (BinaryCreator(Equal)) or
            keywordOf("!=") + sum + (BinaryCreator(NotEqual))
    ).opt()

    val logicalLit = keywordOf("not") + cmp + unaryCreator(Not) or cmp
    val logicalAnd = logicalLit + (keywordOf("and") + logicalLit + (BinaryCreator(And))).rep()
    val logicalXor = logicalAnd + (keywordOf("xor") + logicalAnd + (BinaryCreator(Xor))).rep()
    val logicalOr = logicalXor + (keywordOf("or") + logicalXor + (BinaryCreator(Or))).rep()

    init { expr.ref = logicalOr }

    val exprstmt = expr + (keywordOf("=") + expr + (Assignment.Creator)).opt()

    val whilestmt = "while".ref(Annot.Keyword) +
        "(" + expr + ")" + (
            (stmt orSwapOnPrint Nop.Creator) + While.Creator
        )


    val forstmt = "for".ref(Annot.Keyword) + "(" +
            identifier + "in" + (expr + PairCreator<String, Node>()) + ")" +
            (stmt + For.Creator)

    init { stmt.ref = whilestmt or forstmt or exprstmt }

    val valdecl = "val".ref(Annot.DefKeyword) + identifier + "=" + (expr + ValDecl.Creator)


    val varParameter = "var".ref(Annot.DefKeyword) + identifier + (
            keywordOf(":") + identifier + (VarParameter.CreatorWithType) or
            VarParameter.CreatorWithoutType
    )

    val parameters = (varParameter or idNode).rep(comma)

    val signature: Parser<Pair<String, List<Node>>> = identifier + "(" + (parameters + PairCreator<String, List<Node>>()) + ")"

    val fundecl = "fun".ref(Annot.DefKeyword) + signature +
            ((block or keywordOf("=") + expr) + FunDecl.Creator)

    val classSignature: Parser<Pair<String, List<Node>>> = identifier + (
            keywordOf("(") + parameters + ")" orSwapOnPrint EmptyListCreator()
    )

    val classdecl = "class".ref(Annot.DefKeyword) +
            classSignature +
            (block + ClassDecl.Creator)

    val externdecl = "extern".ref(Annot.DefKeyword) + identifier +
            (keywordOf(":") + expr + (PairCreator<String, Node>())) +
            "=" + (str + ExternDecl.Creator)

    val vardecl = varParameter + (
            keywordOf("=") + expr + (VarDecl.CreatorWithInit) or
            VarDecl.CreatorWithoutInit
    )

    val decl = vardecl or valdecl or fundecl or classdecl or externdecl

    val semicolon = keywordOf(";") + Mapping.identity<Node>()

    val stmtOrDecl = ((decl or stmt) + (semicolon orSwapOnPrint SkipSemicolon)).ref(Annot.Stmt)

    init { stmts.ref = stmtOrDecl.rep() }

    val program = stmts + Block.Creator + eof
}