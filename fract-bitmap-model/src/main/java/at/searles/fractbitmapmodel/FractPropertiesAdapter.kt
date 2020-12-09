package at.searles.fractbitmapmodel

import android.os.Bundle
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.FractlangProgram
import at.searles.paletteeditor.PaletteAdapter
import org.json.JSONArray
import org.json.JSONObject

/**
 * Serialization
 */
object FractPropertiesAdapter {
    fun toBundle(props: FractProperties): Bundle {
        val bundle = Bundle()

        if(!props.isDefaultScale) {
            bundle.putDoubleArray(scaleKey, scaleToArray(props.scale))
        }

        bundle.putBundle(parametersKey, parametersToBundle(props.customParameters))

        bundle.putBundle(palettesKey, palettesToBundle(props.customPalettes))

        if(!props.isDefaultShaderProperties) {
            bundle.putBundle(shaderPropertiesKey, props.shaderProperties.toBundle())
        }

        bundle.putString(sourceCodeKey, props.sourceCode)

        return bundle
    }

    fun fromBundle(bundle: Bundle): FractProperties {
        val scale = bundle.getDoubleArray(scaleKey)?.let {
            scaleFromArray(it)
        }

        val parameters = parametersFromBundle(bundle.getBundle(parametersKey)!!)

        val palettes = palettesFromBundle(bundle.getBundle(palettesKey)!!)

        val shaderProperties = bundle.getBundle(shaderPropertiesKey)?.let {
            ShaderProperties.fromBundle(it)
        }

        val sourceCode = bundle.getString(sourceCodeKey)!!

        val program = FractlangProgram(sourceCode, parameters)

        return FractProperties(program, scale, shaderProperties, palettes)
    }

    fun toJson(props: FractProperties): JSONObject {
        val obj = JSONObject()

        if(!props.isDefaultScale) {
            obj.put(scaleKey, scaleToJson(props.scale))
        }

        obj.put(parametersKey, JSONObject(props.customParameters))

        obj.put(palettesKey, palettesToJson(props.customPalettes))

        if(!props.isDefaultShaderProperties) {
            obj.put(shaderPropertiesKey, props.shaderProperties.toJson())
        }

        obj.put(sourceCodeKey, props.sourceCode)

        return obj
    }

    private fun palettesToJson(palettes: Map<String, Palette>): JSONObject {
        val palettesObj = JSONObject()

        palettes.forEach { (label, palette) ->
            palettesObj.put(label, PaletteAdapter.toJson(palette))
        }

        return palettesObj
    }

    private fun palettesToBundle(palettes: Map<String, Palette>): Bundle {
        val bundle = Bundle()
        palettes.forEach { (label, palette) ->
            bundle.putBundle(label, PaletteAdapter.toBundle(palette))
        }

        return bundle
    }

    private fun palettesFromBundle(bundle: Bundle): Map<String, Palette> {
        val paletteMap = HashMap<String, Palette>()

        bundle.keySet().forEach {
            paletteMap[it] = PaletteAdapter.toPalette(bundle.getBundle(it)!!)
        }

        return paletteMap
    }

    private fun scaleToJson(scale: Scale): JSONArray {
        val scaleArray = JSONArray()

        // order consistent with constructor in Scale
        scaleArray.put(scale.xx).put(scale.xy).put(scale.yx).put(scale.yy).put(scale.cx).put(scale.cy)

        return scaleArray
    }

    fun fromJson(obj: JSONObject): FractProperties {
        val customParameters = parametersFromJson(obj.getJSONObject(parametersKey))

        val sourceCode = obj.getString(sourceCodeKey)

        val program = FractlangProgram(sourceCode, customParameters)

        val customScale: Scale? = if(obj.has(scaleKey)) {
            scaleFromJson(obj.getJSONArray(scaleKey))
        } else {
            null
        }

        // Due to a change in palettes, this branching was needed.
        val customPalettes: Map<String, Palette> = obj.optJSONArray(palettesKey)?.let {
            palettesFromJsonArray(it)
        } ?: palettesFromJsonObj(obj.getJSONObject(palettesKey))

        val customShaderProperties: ShaderProperties? = if(obj.has(shaderPropertiesKey)) {
            ShaderProperties.fromJson(obj.getJSONObject(shaderPropertiesKey))
        } else {
            null
        }

        return FractProperties.create(program, customScale, customShaderProperties, customPalettes)
    }

    fun palettesFromJsonObj(palettesObj: JSONObject): Map<String, Palette> {
        val paletteMap = HashMap<String, Palette>()

        val keys = palettesObj.keys()

        while(keys.hasNext()) {
            val key = keys.next()
            val palette = PaletteAdapter.toPalette(palettesObj.getJSONObject(key))
            paletteMap[key] = palette
        }

        return paletteMap
    }

    /**
     * This is used for Jsons that were created before Version 1.0.3
     */
    fun palettesFromJsonArray(palettesArray: JSONArray): Map<String, Palette> {
        val paletteMap = HashMap<String, Palette>()

        for(i in 0 until palettesArray.length()) {
            if(!palettesArray.isNull(i)) {
                val palette = PaletteAdapter.toPalette(palettesArray.getJSONObject(i))
                paletteMap["$i"] = palette
            }
        }

        return paletteMap
    }

    fun scaleFromJson(scaleArray: JSONArray): Scale {
        return Scale(
            scaleArray.getDouble(0),
            scaleArray.getDouble(1),
            scaleArray.getDouble(2),
            scaleArray.getDouble(3),
            scaleArray.getDouble(4),
            scaleArray.getDouble(5))
    }

    fun scaleFromArray(scaleArray: DoubleArray): Scale {
        return Scale(
            scaleArray[0],
            scaleArray[1],
            scaleArray[2],
            scaleArray[3],
            scaleArray[4],
            scaleArray[5])
    }

    fun scaleToArray(scale: Scale): DoubleArray {
        return doubleArrayOf(
            scale.xx, scale.xy, scale.yx, scale.yy, scale.cx, scale.cy
        )
    }

    fun parametersFromJson(obj: JSONObject): Map<String, String> {
        // parametersToJson is "JSONObject(parameters)"
        val parameters = HashMap<String, String>()
        obj.keys().forEach { key -> parameters[key] = obj.getString(key) }

        return parameters
    }

    fun parametersToBundle(parameters: Map<String, String>): Bundle {
        val bundle = Bundle()

        parameters.forEach { (key, value) ->
            bundle.putString(key, value)
        }

        return bundle
    }

    fun parametersFromBundle(bundle: Bundle): Map<String, String> {
        return bundle.keySet().map { it to bundle.getString(it)!! }.toMap()
    }

    const val sourceCodeKey = "sourceCode"
    const val parametersKey = "parameters"
    const val scaleKey = "scale"

    const val palettesKey = "palettes" // this is for palette lists.
    const val shaderPropertiesKey = "shaderProperties"

}