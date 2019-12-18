package at.searles.fract.changes

import at.searles.fractbitmapmodel.CalculationTaskFactory
import at.searles.fractbitmapmodel.tasks.PostCalculationTask

class SourceCodeChangeTask(private val sourceCode: String): PostCalculationTask {
    override val isParameterChange: Boolean = true

    override fun execute(preferences: CalculationTaskFactory) {
        preferences.bitmapModelParameters = preferences.bitmapModelParameters.createWithNewSourceCode(sourceCode)
    }
}