package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.BitmapAllocation
import at.searles.fractbitmapmodel.FractBitmapModel

class BitmapAllocationChange(private val newBitmapAllocation: BitmapAllocation): BitmapModelChange {
    override fun accept(model: FractBitmapModel) {
        model.bitmapAllocation = newBitmapAllocation
    }
}