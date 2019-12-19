package at.searles.fract.changes

import at.searles.fractbitmapmodel.CalcController
import at.searles.fractbitmapmodel.tasks.PostCalcChange

class SetAssetChange(private val sourceCode: String, private val parameters: Map<String, String>): PostCalcChange {
    override fun accept(controller: CalcController) {
        controller.calcProperties = controller.calcProperties.createWithNewAsset(sourceCode, parameters)
    }
}