package at.searles.fractlang.linear

import at.searles.fractlang.Type

class Alloc(val id: String, val type: Type): CodeLine {
    init {
        require(type.vmCodeSize() != 0)
    }

    // purely a marker to properly handle var assignments.
    override fun toString(): String {
        return "Allocate $id: $type"
    }
}
