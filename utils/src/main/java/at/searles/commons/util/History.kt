package at.searles.commons.util

import java.util.*

/**
 * Idea is
 * [b0, b1] current [f2, f1, f0]
 *
 *
 *
 */
class History<A> {
    private val historyBack = Stack<A>()
    private var current: A? = null
    private val historyForward = Stack<A>()

    fun add(item: A) {
        if(current != null) {
            historyBack.push(current)
        }

        current = item
    }

    fun hasBack(): Boolean = !historyBack.empty()

    fun hasForward(): Boolean = !historyForward.empty()

    fun back(): A {
        if(current != null) {
            historyForward.push(current)
        }

        current = historyBack.pop()
        return current!!
    }

    fun forward(): A {
        if(current != null) {
            historyBack.push(current)
        }

        current = historyForward.pop()
        return current!!
    }
}

