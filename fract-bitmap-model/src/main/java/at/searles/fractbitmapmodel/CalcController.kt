package at.searles.fractbitmapmodel

import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import kotlin.math.max

/**
 * This class has 3 purposes:
 *
 * + Create CalcTasks (requires CalcScript, width, height, calcData from BitmapAllocation)
 * + Mediator between properties and this.
 * +
 * contains the scripts for calc. It also maintains
 *
 *
 */
class CalcController(val rs: RenderScript) {

    private var calcScript: ScriptC_calc? = null

    private var codeAllocation: Allocation = Allocation.createSized(rs, Element.I32(rs), 1)

    fun getCalcScript(): Lazy<ScriptC_calc> {
        return lazy { calcScript!! }
    }

    fun initialize() {
        calcScript = ScriptC_calc(rs)
    }

    fun updateVmCodeInScript(properties: FractProperties) {
        if(calcScript == null) {
            // it will be initialized later.
            return
        }

        val vmCode = properties.vmCode

        if(vmCode.isNotEmpty()) {
            codeAllocation.destroy()
            codeAllocation = Allocation.createSized(rs, Element.I32(rs), vmCode.size)
            codeAllocation.copyFrom(vmCode)
            calcScript!!.bind_code(codeAllocation)
        }

        calcScript!!._codeSize = vmCode.size.toLong()
    }

    fun updateScale(properties: FractProperties, width: Int, height: Int) {
        if(calcScript == null) {
            // it will be initialized later.
            return
        }

        val centerX = width / 2.0
        val centerY = height / 2.0
        val factor = 1.0 / if (centerX < centerY) centerX else centerY

        val scale = properties.scale

        val scriptScale =  ScriptField_Scale.Item().apply {
            a = scale.xx * factor
            b = scale.yx * factor
            c = scale.xy * factor
            d = scale.yy * factor

            e = scale.cx - (a * centerX + b * centerY)
            f = scale.cy - (c * centerX + d * centerY)
        }

        calcScript!!._scale = scriptScale
    }

    companion object {
        const val parallelCalculationsCount = 8192
    }
}