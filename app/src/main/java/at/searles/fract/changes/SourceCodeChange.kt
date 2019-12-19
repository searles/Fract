package at.searles.fract.changes

import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.tasks.PostCalcChange

class SourceCodeChange(private val sourceCode: String): PostCalcChange {
    override fun accept(controller: CalcController) {
        try {
            controller.calcProperties = controller.calcProperties.createWithNewSourceCode(sourceCode)
        } catch(th: Throwable) {
            // FIXME
        }
    }
}