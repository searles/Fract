package at.searles.fractlang

import at.searles.fractlang.nodes.*

interface Visitor<T> {
    fun visit(app: App): T
    fun visit(block: Block): T
    fun visit(classDecl: ClassDecl): T
    fun visit(forStmt: For): T
    fun visit(funDecl: FunDecl): T
    fun visit(idNode: IdNode): T
    fun visit(ifStmt: If): T
    fun visit(ifElse: IfElse): T
    fun visit(intNode: IntNode): T
    fun visit(qualifiedNode: QualifiedNode): T
    fun visit(realNode: RealNode): T
    fun visit(stringNode: StringNode): T
    fun visit(varDecl: VarDecl): T
    fun visit(varParameter: VarParameter): T
    fun visit(vectorNode: VectorNode): T
    fun visit(whileStmt: While): T
    fun visit(classEnv: ClassEnv): T
    fun visit(funEnv: FunEnv): T
    fun visit(cplxNode: CplxNode): T
    fun visit(boolNode: BoolNode): T
    fun visit(nop: Nop): T
    fun visit(opNode: OpNode): T
    fun visit(objectNode: ObjectNode): T
    fun visit(valDecl: ValDecl): T
    fun visit(assignment: Assignment): T
    fun visit(externDecl: ExternDecl): T
    fun visit(externNode: ExternNode): T
    fun visit(indexedNode: IndexedNode): T
    fun visit(appChain: AppChain): T
}