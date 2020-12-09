package at.searles.paletteeditor.paletteeditorview

import android.graphics.Canvas
import android.graphics.Paint
import android.view.DragEvent
import android.view.MotionEvent
import at.searles.commons.color.Colors
import at.searles.multiscrollview.InnerPane
import at.searles.multiscrollview.InnerPaneView
import at.searles.multiscrollview.ScrollDirection
import at.searles.paletteeditor.Dpis
import at.searles.paletteeditor.ThemeUtils
import kotlin.math.hypot

abstract class OffsetPane(private val rootView: InnerPaneView, private val paletteEditorPane: PaletteEditorPane): InnerPane {
    val model
        get() = paletteEditorPane.model

    private val iconSize
        get() = paletteEditorPane.iconSize

    val sliderIconSize
        get() = iconSize / 1.5f

    val activeColor = ThemeUtils.getThemeAccentColor(rootView.context)
    val passiveColor = Colors.toGray(activeColor)

    private var isDragging = false

    abstract fun updateOffsetFromEvent(e: MotionEvent, visibleX0: Float, visibleY0: Float)

    override fun onClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onTapDown(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        val d = hypot((e.x - sliderIconX(visibleX0)).toDouble(), (e.y - sliderIconY(visibleY0)).toDouble())

        if(2.0 * d < iconSize) {
            isDragging = true
            rootView.invalidate()
            return true
        }

        return false
    }

    protected abstract val scrollDirection: ScrollDirection

    override fun onScrollTo(e: MotionEvent, visibleX0: Float, visibleY0: Float): ScrollDirection {
        if(isDragging) {
            updateOffsetFromEvent(e, visibleX0, visibleY0)
            rootView.invalidate()
            return scrollDirection
        }

        return ScrollDirection.NoScroll
    }

    override fun onLongPress(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun onTapUp(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        if(isDragging) {
            isDragging = false
            rootView.invalidate()
            return true
        }

        return false
    }

    override fun onDoubleClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    abstract fun sliderIconX(visibleX0: Float): Float
    abstract fun sliderIconY(visibleY0: Float): Float

    protected val sliderRulerPaint = Paint().apply{ strokeWidth = Dpis.dpiToPx(rootView.resources, rulerWidthDp)}

    abstract fun drawRuler(canvas: Canvas, visibleX0: Float, visibleY0: Float)

    override fun onDraw(canvas: Canvas, visibleX0: Float, visibleY0: Float, visibleWidth: Float, visibleHeight: Float) {
        drawRuler(canvas, visibleX0, visibleY0)

        val sliderIcon = rootView.resources.getDrawable(sliderIcon, null)

        sliderIcon.setTint(if(isDragging) activeColor else passiveColor)

        val cx = sliderIconX(visibleX0)
        val cy = sliderIconY(visibleY0)

        sliderIcon.setBounds(
            (cx - sliderIconSize / 2f).toInt(),
            (cy - sliderIconSize / 2f).toInt(),
            (cx + sliderIconSize / 2f).toInt(),
            (cy + sliderIconSize / 2f).toInt()
        )

        sliderIcon.draw(canvas)
    }

    override fun dragStarted(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragEntered(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragExited(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun dragLocation(e: DragEvent, visibleX0: Float, visibleY0: Float): ScrollDirection? {
        return null
    }

    override fun dragEnded(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }

    override fun drop(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return false
    }


    companion object {
        const val rulerWidthDp = 4f
    }

    abstract val sliderIcon: Int
}