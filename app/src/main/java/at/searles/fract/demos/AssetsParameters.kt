package at.searles.fract.demos

import at.searles.commons.math.Scale

class AssetsParameters(
    private val parameterKey: String, val title: String, private val description: String,
    val tags: Set<String>,
    val scale: Scale?,
    val parameters: Map<String, String>) {
    fun createItem(sourceKey: String): AssetsParametersItem {
        return AssetsParametersItem(sourceKey, parameterKey, title, description, scale, parameters)
    }
}