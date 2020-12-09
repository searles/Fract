package at.searles.multiscrollview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.DragEvent
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import at.searles.paletteeditor.R
import java.util.*


/**
 * Thanks to http://stackoverflow.com/questions/12074950/android-horizontalscrollview-inside-scrollview
 */
class MultiScrollView(context: Context, attributeSet: AttributeSet) : LinearLayout(context, attributeSet) {

    private var borderScrollMargin = dpToPix(context.resources, borderScrollMarginDp)

    private val initialBorderScrollStepSize
        get() = borderScrollMargin / 5f
    private val maxBorderScrollStepSize
        get() = borderScrollMargin * 2f

    private val borderScrollUpdateDelayMs = 40L
    private val borderScrollAcceleration = 1.05f

    private val hscroll: HorizontalScrollView by lazy {
        findViewById<HorizontalScrollView>(R.id.hscroll)
    }

    private val vscroll: ScrollView by lazy {
        findViewById<ScrollView>(R.id.vscroll)
    }

    // spacings so that the scrolls will be of correct size
    private val hspace: View by lazy {
        findViewById<View>(R.id.hspace)
    }

    private val vspace: View by lazy {
        findViewById<View>(R.id.vspace)
    }

    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, GestureController())
    }

    private val innerPaneView: InnerPaneView by lazy {
        findViewById<View>(R.id.innerPaneView) as InnerPaneView
    }

    init {
        View.inflate(context,
            R.layout.multiscroll_view, this)

        hscroll.viewTreeObserver.addOnScrollChangedListener { updateViewCoordinates() }
        vscroll.viewTreeObserver.addOnScrollChangedListener { updateViewCoordinates() }

        post {
            updateSize()
            updateViewCoordinates()
        }
    }

    private var currentScrollStepSize = 0f

    private var scrollDirectionX = 0f
    private var scrollDirectionY = 0f

    private var scrollDragTimer: Timer? = null

    public override fun onDraw(canvas: Canvas) {
        innerPaneView.draw(canvas)
    }

    private fun initiateBorderScrollIfNecessary(x: Float, y: Float, scrollDirection: ScrollDirection?) {
        if(scrollDirection != null && scrollDirection != ScrollDirection.NoScroll) {
            if(!isBorderScrolling(x, y, scrollDirection)) {
                cancelBorderScrolling()
            }
        }
    }

    override fun dispatchDragEvent(e: DragEvent): Boolean {
        return when(e.action) {
            DragEvent.ACTION_DRAG_STARTED -> { innerPaneView.dragStarted(e) }
            DragEvent.ACTION_DRAG_ENTERED -> { innerPaneView.dragEntered(e) }
            DragEvent.ACTION_DRAG_EXITED -> { innerPaneView.dragExited(e) }
            DragEvent.ACTION_DRAG_LOCATION -> {
                val scrollDirection = innerPaneView.dragLocation(e)
                initiateBorderScrollIfNecessary(e.x, e.y, scrollDirection)
                return scrollDirection != null
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                cancelBorderScrolling()
                innerPaneView.dragEnded(e)
            }
            DragEvent.ACTION_DROP -> { innerPaneView.drop(e) }
            else -> error("unknown action")
        }
    }



    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_MOVE) {
            val scrollDirection = innerPaneView.onScrollTo(e)

            if(scrollDirection != ScrollDirection.NoScroll) {
                if(isBorderScrolling(e.x, e.y, scrollDirection)) {
                    return true
                } else {
                    cancelBorderScrolling()
                }

                return true
            }
        } else if (e.action == MotionEvent.ACTION_UP) {
            cancelBorderScrolling()
            if (innerPaneView.onTapUp(e)) {
                return true
            }
        } else if (e.action == MotionEvent.ACTION_DOWN) {
            if (innerPaneView.onTapDown(e)) {
                return true
            }
        }

        return gestureDetector.onTouchEvent(e)
                || (hscroll.dispatchTouchEvent(e) or vscroll.dispatchTouchEvent(e))
    }

    private fun isBorderScrolling(x: Float, y: Float, scrollDirection: ScrollDirection): Boolean {
        val dx = when {
            scrollDirection.isHorizontal && x < borderScrollMargin -> x - borderScrollMargin
            scrollDirection.isHorizontal && x > width - borderScrollMargin -> x - width + borderScrollMargin
            else -> 0f
        }

        val dy = when {
            scrollDirection.isVertical && y < borderScrollMargin -> y - borderScrollMargin
            scrollDirection.isVertical && y > height - borderScrollMargin -> y - height + borderScrollMargin
            else -> 0f
        }

        if(dx == 0f && dy == 0f) {
            return false
        }

        scrollDirectionX = dx / borderScrollMargin
        scrollDirectionY = dy / borderScrollMargin

        if(scrollDragTimer == null) {
            scrollDragTimer = Timer()
            currentScrollStepSize = initialBorderScrollStepSize

            scrollDragTimer!!.scheduleAtFixedRate(
                BorderScrollUpdateTask(),
                0,
                borderScrollUpdateDelayMs
            )
        }

        return true
    }

    private fun cancelBorderScrolling() {
        scrollDragTimer?.cancel()
        scrollDragTimer = null
    }

    private fun updateViewCoordinates() {
        innerPaneView.visibleX0 = hscroll.scrollX.toFloat()
        innerPaneView.visibleY0 = vscroll.scrollY.toFloat()
    }

    fun updateSize() {
        hspace.minimumWidth = innerPaneView.intendedWidth.toInt()
        hspace.layoutParams.width = innerPaneView.intendedWidth.toInt()

        vspace.minimumHeight = innerPaneView.intendedHeight.toInt()
        vspace.layoutParams.height = innerPaneView.intendedHeight.toInt()
    }

    private inner class BorderScrollUpdateTask: TimerTask() {
        override fun run() {
            if (currentScrollStepSize < maxBorderScrollStepSize) {
                currentScrollStepSize *= borderScrollAcceleration
            }

            hscroll.scrollX = hscroll.scrollX + (scrollDirectionX * currentScrollStepSize).toInt()
            vscroll.scrollY = vscroll.scrollY + (scrollDirectionY * currentScrollStepSize).toInt()
        }
    }

    private inner class GestureController : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            return innerPaneView.onClick(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            return innerPaneView.onDoubleClick(e)
        }

        override fun onLongPress(e: MotionEvent) {
            innerPaneView.onLongPress(e)
        }
    }

    companion object {
        private fun dpToPix(resources: Resources, dp: Float): Float {
            return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        private val borderScrollMarginDp = 96f
    }
}