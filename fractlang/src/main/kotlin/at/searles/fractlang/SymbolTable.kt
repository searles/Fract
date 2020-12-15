package at.searles.fractlang

import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.nodes.Node
import at.searles.parsing.Trace

interface SymbolTable {
    operator fun get(trace: Trace, id: String): Node?

    fun fork(): Mutable {
        return Mutable(this)
    }

    fun addExternValue(trace: Trace, name: String, description: String, expr: String)

    fun setScale(trace: Trace, scale: Scale)

    /**
     * @return The index of the just given palette. Deprecated, use putPalette instead. Just
     * here for legacy purposes
     */
    fun addPalette(trace: Trace, description: String, defaultPalette: Palette): Int

    fun putPalette(trace: Trace, label: String, description: String, defaultPalette: Palette): Int

    class Mutable(private val parent: SymbolTable): SymbolTable {
        private val map: MutableMap<String, Node> = HashMap()

        override fun get(trace: Trace, id: String): Node? {
            return map[id] ?: parent[trace, id]
        }

        override fun addExternValue(trace: Trace, name: String, description: String, expr: String) {
            parent.addExternValue(trace, name, description, expr)
        }

        override fun setScale(trace: Trace, scale: Scale) {
            parent.setScale(trace, scale)
        }

        override fun addPalette(trace: Trace, description: String, defaultPalette: Palette): Int {
            return parent.addPalette(trace, description, defaultPalette)
        }

        override fun putPalette(trace: Trace, label: String, description: String, defaultPalette: Palette): Int {
            return parent.putPalette(trace, label, description, defaultPalette)
        }

        fun set(id: String, value: Node): Boolean {
            if(map.containsKey(id)) {
                return false
            }

            map[id] = value

            return true
        }

        fun top(): Map<String, Node> {
            return map
        }
    }
}
