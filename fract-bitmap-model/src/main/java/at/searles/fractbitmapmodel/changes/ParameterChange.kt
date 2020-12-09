package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException

class ParameterChange(private val key: String, private val newValue: String): CalcPropertiesChange {
    /**
     * @throws SemanticAnalysisException if it causes a compiler error.
     */
    override fun accept(properties: FractProperties): FractProperties {
        val newParameters = properties.customParameters.toMutableMap()

        newParameters[key] = newValue

        val newProgram = FractlangProgram(properties.sourceCode, newParameters)

        return properties.createWithNewProperties(newProgram)
    }
}