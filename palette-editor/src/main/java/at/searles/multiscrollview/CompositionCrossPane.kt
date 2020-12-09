package at.searles.multiscrollview

import android.graphics.Canvas
import android.view.DragEvent
import android.view.MotionEvent

/**
 * Cross-shaped composed inner pane
 *       top
 * left center right
 *      bottom
 */
class CompositionCrossPane(private val leftPane: InnerPane, private val topPane: InnerPane, private val rightPane: InnerPane, private val bottomPane: InnerPane, private val centerPane: InnerPane): InnerPane {
    override val width: Float
        get() = leftPane.width + centerPane.width + rightPane.width
    override val height: Float
        get() = topPane.height + centerPane.height + bottomPane.height

    private val order = listOf(
        Triple(leftPane, { visibleX0: Float -> visibleX0 }, { visibleY0: Float -> visibleY0 - topPane.height }),
        Triple(topPane, { visibleX0: Float -> visibleX0 - leftPane.width }, { visibleY0: Float -> visibleY0 }),
        Triple(rightPane, { visibleX0: Float -> visibleX0 - leftPane.width - centerPane.width }, { visibleY0: Float -> visibleY0 - topPane.height }),
        Triple(bottomPane, { visibleX0: Float -> visibleX0 - leftPane.width }, { visibleY0: Float -> visibleY0 - topPane.height - centerPane.height }),
        Triple(centerPane, { visibleX0: Float -> visibleX0 - leftPane.width }, { visibleY0: Float -> visibleY0 - topPane.height })
    )

    override fun onClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.onClick(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun onTapDown(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.onTapDown(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun onScrollTo(e: MotionEvent, visibleX0: Float, visibleY0: Float): ScrollDirection {
        return order.asSequence().map {
            it.first.onScrollTo(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0))
        }.firstOrNull {
            it != ScrollDirection.NoScroll
        } ?: ScrollDirection.NoScroll
    }

    override fun onLongPress(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.onLongPress(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun onTapUp(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.onTapUp(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun onDoubleClick(e: MotionEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.onDoubleClick(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun onDraw(canvas: Canvas, visibleX0: Float, visibleY0: Float, visibleWidth: Float, visibleHeight: Float) {
        order.reversed().forEach {
            it.first.onDraw(canvas, it.second.invoke(visibleX0), it.third.invoke(visibleY0), visibleWidth, visibleHeight)
        }
    }

    override fun dragStarted(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.dragStarted(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun dragEntered(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.dragEntered(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun dragExited(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.dragExited(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun dragLocation(e: DragEvent, visibleX0: Float, visibleY0: Float): ScrollDirection? {
        return order.asSequence().map { it.first.dragLocation(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it != null }
    }

    override fun dragEnded(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.dragEnded(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }

    override fun drop(e: DragEvent, visibleX0: Float, visibleY0: Float): Boolean {
        return order.asSequence().map { it.first.drop(e, it.second.invoke(visibleX0), it.third.invoke(visibleY0)) }.firstOrNull { it } ?: false
    }
}