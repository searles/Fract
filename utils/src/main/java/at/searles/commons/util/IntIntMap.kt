package at.searles.commons.util

import java.util.*

class IntIntMap<A>: Iterable<IntIntMap.Entry<A>> {

    private val map = HashMap<Long, Entry<A>>()

    operator fun get(x: Int, y: Int): A? {
        return map[compose(x, y)]?.value
    }

    fun getValue(x: Int, y: Int): A {
        return this[x, y]!!
    }

    override fun iterator(): Iterator<Entry<A>> {
        return map.values.iterator()
    }

    fun containsKey(x: Int, y: Int): Boolean {
        return map.containsKey(compose(x, y))
    }

    fun isEmpty(): Boolean {
        return map.isEmpty()
    }

    fun size(): Int {
        return map.size
    }

    operator fun set(x: Int, y: Int, value: A) {
        map[compose(x, y)] = Entry(x, y, value)
    }

    fun remove(x: Int, y: Int): A? {
        return map.remove(compose(x, y))?.value
    }

    fun clear() {
        map.clear()
    }

    class Entry<A>(val x: Int, val y: Int, val value: A)

    companion object {
        private fun compose(x: Int, y: Int): Long {
            return x.toLong().shl(32).or(
                    y.toLong().and(0xffffffff)
            )
        }
    }
}