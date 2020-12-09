package at.searles.fractimageview.demo

import android.os.Bundle
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import at.searles.fractimageview.plugins.DrawBitmapBoundsPlugin
import at.searles.fractimageview.plugins.GridPlugin
import at.searles.fractimageview.plugins.IconIfFlippedPlugin
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractimageview.plugins.GestureBlockPlugin

/**
 * TODO: Center lock
 */
class DemoActivity : AppCompatActivity() {

    private val scalableImageView: PluginScalableImageView by lazy {
        findViewById<PluginScalableImageView>(R.id.scalableImageView)
    }

    private val textView: TextView by lazy {
        findViewById<TextView>(R.id.textView)
    }

    private val rotationLockCheckBox: CheckBox by lazy {
        findViewById<CheckBox>(R.id.rotationLockCheckBox)
    }

    private val centerLockCheckBox: CheckBox by lazy {
        findViewById<CheckBox>(R.id.centerLockCheckBox)
    }

    private val confirmZoomCheckBox: CheckBox by lazy {
        findViewById<CheckBox>(R.id.confirmZoomCheckBox)
    }

    private val isTouchEnabledCheckBox: CheckBox by lazy {
        findViewById<CheckBox>(R.id.isTouchEnabledCheckBox)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scalableImageView.bitmapModel = DemoBitmapProvider()

        scalableImageView.addPlugin(DrawBitmapBoundsPlugin())
        scalableImageView.addPlugin(
            GridPlugin(
                this
            )
        )
        scalableImageView.addPlugin(DemoPlugin(textView))
        scalableImageView.addPlugin(IconIfFlippedPlugin(this))

        val touchBlocker = GestureBlockPlugin()

        scalableImageView.addPlugin(touchBlocker)

        confirmZoomCheckBox.setOnClickListener { scalableImageView.mustConfirmZoom = confirmZoomCheckBox.isChecked }
        rotationLockCheckBox.setOnClickListener { scalableImageView.hasRotationLock = rotationLockCheckBox.isChecked }
        centerLockCheckBox.setOnClickListener { scalableImageView.hasCenterLock = centerLockCheckBox.isChecked }
        isTouchEnabledCheckBox.setOnClickListener { touchBlocker.isEnabled = !isTouchEnabledCheckBox.isChecked }

        scalableImageView.mustConfirmZoom = confirmZoomCheckBox.isChecked
        scalableImageView.hasRotationLock = rotationLockCheckBox.isChecked
        scalableImageView.hasCenterLock = centerLockCheckBox.isChecked
    }

    override fun onBackPressed() {
        scalableImageView.cancelMultitouchGesture()
    }
}
