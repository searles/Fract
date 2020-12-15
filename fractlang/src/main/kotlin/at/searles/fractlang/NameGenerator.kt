package at.searles.fractlang

class NameGenerator {
    private val baseMapCounter = HashMap<String, Int>()

    fun next(base: String): String {
        // strip trailing numbers from base
        val index = baseMapCounter.getOrPut(base, {0})
        baseMapCounter[base] = index + 1
        return if(index == 0) base else "$base\$$index"
    }
}