package at.searles.fractbitmapmodel

import android.graphics.Matrix
import android.os.Looper
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.util.Log
import at.searles.commons.util.History
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.ScalableBitmapModel

/**
 * Represents bitmap models that depend on potentially long running tasks and therefore
 * are not able to provide an instant review. For this cases a second relative scale matrix
 * is maintained inside and used after a first preview is available.
 */
class FractBitmapModel(
    val rs: RenderScript,
    initialBitmapAllocation: BitmapAllocation,
    initialProperties: FractProperties): CalcTask.Listener, ScalableBitmapModel() {

    var bitmapAllocation = initialBitmapAllocation
        set(value) {
            field = value
            updateScaleInScripts()
            bitmapController.bitmapAllocation = bitmapAllocation
        }

    val renderSegment: Allocation = Allocation.createSized(rs, Element.F32_3(rs),
        CalcController.parallelCalculationsCount
    )

    /*
     * Must be initialized before starting a calculation. This will happen in the
     * first call to startTask.
     */
    private val calcController = CalcController(rs)
    private val bitmapController = BitmapController(rs, initialBitmapAllocation).apply {
        this.listener = object: BitmapController.Listener {
            override fun bitmapUpdated() {
                Log.d("FractBitmapModel.BitmapController.Listener", "bitmap was updated")
                this@FractBitmapModel.listener?.bitmapUpdated()
            }
        }
    }

    override val bitmapTransformMatrix = Matrix()
    override val bitmap get() = bitmapAllocation.bitmap

    var listener: Listener? = null

    private var nextBitmapTransformMatrix: Matrix = Matrix()

    private var isWaitingForPreview: Boolean = false

    var isTaskRunning: Boolean = false
        private set

    private var calcTask: CalcTask? = null

    private val bitmapModelChanges = ArrayList<BitmapModelChange>()

    var properties: FractProperties = initialProperties
    private var calcPropertiesChanged = false

    private var isInitialized = false

    private val history = History<FractProperties>().apply {
        add(initialProperties)
    }

    override fun scale(relativeMatrix: Matrix) {
        require(Looper.getMainLooper().isCurrentThread)

        bitmapTransformMatrix.postConcat(relativeMatrix)
        nextBitmapTransformMatrix.postConcat(relativeMatrix)

        scheduleCalcPropertiesChange(RelativeScaleChange(relativeMatrix))
    }

    fun scheduleBitmapUpdate() {
        if(isInitialized) {
            bitmapController.scheduleBitmapUpdate()
        }
    }

    private var lastPixelGap = -1

    override fun setProgress(progress: Float) {
        if(isWaitingForPreview) {
            bitmapTransformMatrix.set(nextBitmapTransformMatrix)
            isWaitingForPreview = false
            Log.d("FractBitmapModel.setProgress", "first preview")
            scheduleBitmapUpdate()
            lastPixelGap = bitmapAllocation.pixelGap
        } else if(lastPixelGap != bitmapAllocation.pixelGap) {
            Log.d("FractBitmapModel.setProgress", "pixelgap changed from $lastPixelGap to ${bitmapAllocation.pixelGap}")
            scheduleBitmapUpdate()
            lastPixelGap = bitmapAllocation.pixelGap
        }

        listener?.setProgress(progress)
    }

    override fun finished() {
        isTaskRunning = false
        calcTask = null

        listener?.finished()

        val hasBitmapModelChange = bitmapModelChanges.isNotEmpty()

        if(hasBitmapModelChange) {
            bitmapModelChanges.forEach { it.accept(this) }
            bitmapModelChanges.clear()
        }

        if(calcPropertiesChanged) {
            updatePropertiesInScript()
        }

        if(hasBitmapModelChange || calcPropertiesChanged) {
            calcPropertiesChanged = false
            listener?.propertiesChanged(this)
            startTask()
        }
    }

    fun applyBitmapPropertiesChange(change: BitmapPropertiesChange) {
        require(Looper.getMainLooper().isCurrentThread)
        properties = change.accept(properties)
        updateBitmapParametersInScripts()
        scheduleBitmapUpdate()
    }

    fun scheduleCalcPropertiesChange(change: CalcPropertiesChange) {
        require(Looper.getMainLooper().isCurrentThread)

        properties = change.accept(properties)

        if(change.addToHistory) {
            history.add(properties)
        }

        if(isTaskRunning) {
            calcPropertiesChanged = true
            calcTask!!.cancel(true)
            return
        }

        updatePropertiesInScript()
        listener?.propertiesChanged(this)
        startTask()
    }

    fun scheduleBitmapModelChange(change: BitmapModelChange) {
        if(isTaskRunning) {
            bitmapModelChanges.add(change)
            calcTask!!.cancel(true)
            return
        }

        change.accept(this)
        listener?.propertiesChanged(this)
        startTask()
    }

    fun startAnimation(maxResolution: Int) {
        bitmapController.startAnimation(maxResolution)
    }

    fun stopAnimation() {
        bitmapController.stopAnimation()
    }

    fun hasBackHistory() = history.hasBack()

    fun hasForwardHistory() = history.hasForward()

    fun historyBack() {
        scheduleCalcPropertiesChange(NewFractPropertiesChange(history.back(), false))
    }

    fun historyForward() {
        scheduleCalcPropertiesChange(NewFractPropertiesChange(history.forward(), false))
    }

    /**
     * Sets new properties
     */
    private fun updatePropertiesInScript() {
        require(!isTaskRunning)
        calcController.updateVmCodeInScript(properties)
        updateScaleInScripts()

        updateBitmapParametersInScripts()
    }

    private fun updateBitmapParametersInScripts() {
        if(!isInitialized) {
            // they will be initialized later.
            return
        }

        bitmapController.updateShaderProperties(properties)
        bitmapController.updatePalettes(properties)
    }

    fun initialize() {
        if(!isInitialized) {
            calcController.initialize()
            calcController.updateVmCodeInScript(properties)
            updateScaleInScripts()

            bitmapController.initialize()
            bitmapController.updatePalettes(properties)
            bitmapController.updateShaderProperties(properties)

            isInitialized = true
        }
    }

    /**
     * Synchronizes scale value between bitmap scripts and calc script
     */
    private fun updateScaleInScripts() {
        calcController.updateScale(properties, bitmapAllocation.width, bitmapAllocation.height)
    }

    fun startTask() {
        require(Looper.getMainLooper().isCurrentThread)

        require(bitmapModelChanges.isEmpty())
        require(!isTaskRunning)
        require(calcTask == null)

        // Initialize/Reset values
        isWaitingForPreview = true
        isTaskRunning = true

        // before the first preview is generated, we must use the old imageTransformMatrix.
        nextBitmapTransformMatrix.set(null)

        // Now, we start.
        listener?.started()

        startCalcTask()
    }

    private fun startCalcTask() {
        calcTask = CalcTask(rs, this, calcController.getCalcScript()).apply {
            listener = this@FractBitmapModel
            execute()
        }
    }

    interface Listener {
        fun started()
        fun setProgress(progress: Float)
        fun bitmapUpdated()
        fun finished()

        fun propertiesChanged(src: FractBitmapModel)
    }
}