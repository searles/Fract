package at.searles.fractlang

import at.searles.commons.color.Lab
import at.searles.commons.color.Palette
import at.searles.commons.color.Rgb
import at.searles.commons.math.Scale
import at.searles.commons.util.IntIntMap
import at.searles.fractlang.nodes.*
import at.searles.fractlang.ops.MetaOp
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.parsing.Trace

class RootSymbolTable(private val namedInstructions: Map<String, MetaOp>, private val definedParameters: Map<String, String>): SymbolTable {

    /**
     * Content is added to this map
     */
    private val parameterMap = HashMap<String, ExternNode>()
    private val activeParameterKeys = LinkedHashSet<String>()

    // XXX use sorted with Trace Comparator?
    val activeParameters by lazy {
        activeParameterKeys.
            map { parameterMap.getValue(it) }.
            map { it.id to ParameterEntry(it.id, it.description, it.isDefault, it.expr) }.
            toMap()
    }

    var defaultScale: Scale = fallBackScale
        private set

    private val paletteEntries = LinkedHashMap<String, PaletteEntry>()

    val palettes: Map<String, PaletteEntry>
        get() {
            if(paletteEntries.isEmpty()) {
                return fallBackPalettes
            }

            return paletteEntries
        }

    override fun get(trace: Trace, id: String): Node? {
        if(parameterMap.containsKey(id)) {
            activeParameterKeys.add(id)
            return parameterMap[id]
        }

        if(namedInstructions.containsKey(id)) {
            return namedInstructions.getValue(id).toNode(trace)
        }

        return null
    }

    override fun addExternValue(trace: Trace, name: String, description: String, expr: String) {
        val existingEntry = parameterMap[name]

        if(existingEntry != null) {
            if(existingEntry.trace != trace) {
                throw SemanticAnalysisException("extern $name already defined", existingEntry.trace)
            }

            return
        }

        val isDefault = !definedParameters.containsKey(name)

        val node = ExternNode(trace, name, description, isDefault,
            if(isDefault) expr else definedParameters.getValue(name))

        parameterMap[name] = node
    }

    override fun setScale(trace: Trace, scale: Scale) {
        defaultScale = scale
    }

    override fun addPalette(trace: Trace, description: String, defaultPalette: Palette): Int {
        val indexInExisting = paletteEntries.values.indexOfFirst { it.trace == trace }

        if(indexInExisting != -1) {
            return indexInExisting
        }

        val entry = PaletteEntry(trace, paletteEntries.size, description, defaultPalette)
        val index = paletteEntries.size

        paletteEntries["$index"] = entry

        return index
    }

    override fun putPalette(trace: Trace, label: String, description: String, defaultPalette: Palette): Int {
        val existingEntry = paletteEntries[label]

        if(existingEntry != null) {
            return existingEntry.index
        }

        val entry = PaletteEntry(trace, paletteEntries.size, description, defaultPalette)

        paletteEntries[label] = entry

        return entry.index
    }

    class TraceComparator: Comparator<ExternNode> {
        override fun compare(n0: ExternNode, n1: ExternNode): Int {
            val cmp = n0.trace.start.compareTo(n1.trace.start)
            return if(cmp != 0) cmp else n0.trace.end.compareTo(n1.trace.end)
        }
    }

    companion object {
        val fallBackScale = Scale(1.0, 0.0, 0.0, 1.0, 0.0, 0.0)
        val fallBackPalettes = mapOf(
            Pair(
                "Black",
                PaletteEntry(object: Trace {
                    override val start: Long = 0
                    override val end: Long = 0
                }, 0, "Black (no palette defined in program)",
                Palette(1, 1, 0f, 0f, IntIntMap<Lab>().apply {
                    set(0, 0, Rgb(0f, 0f, 0f).toLab())
                })))
        )
    }
}