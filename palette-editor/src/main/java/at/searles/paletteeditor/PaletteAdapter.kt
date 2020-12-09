package at.searles.paletteeditor

import android.os.Bundle
import at.searles.commons.color.Colors
import at.searles.commons.color.Lab
import at.searles.commons.color.Palette
import at.searles.commons.color.Rgb
import at.searles.commons.util.IntIntMap
import org.json.JSONArray
import org.json.JSONObject


object PaletteAdapter {
    fun toPalette(bundle: Bundle) : Palette {
        val width = bundle.getInt(widthKey)
        val height = bundle.getInt(heightKey)
        val offsetX = bundle.getFloat(offsetXKey)
        val offsetY = bundle.getFloat(offsetYKey)

        val colorMap = IntIntMap<Lab>()

        val colorArray = bundle.getIntArray(colorKey)!!

        for(i in colorArray.indices step 3) {
            colorMap[colorArray[i], colorArray[i + 1]] = Rgb.of(colorArray[i + 2]).toLab()
        }

        return Palette(width, height, offsetX, offsetY, colorMap)
    }

    fun toBundle(palette: Palette): Bundle {
        val bundle = Bundle()

        bundle.putInt(widthKey, palette.width)
        bundle.putInt(heightKey, palette.height)
        bundle.putFloat(offsetXKey, palette.offsetX)
        bundle.putFloat(offsetYKey, palette.offsetY)

        val colors: List<Int> = palette.colorPoints.flatMap { entry -> listOf(entry.x, entry.y, entry.value.toRgb().toArgb() )}

        bundle.putIntArray(colorKey, colors.toIntArray())

        return bundle
    }

    fun toJson(palette: Palette): JSONObject {
        val obj = JSONObject()

        obj.put(widthKey, palette.width)
        obj.put(heightKey, palette.height)
        obj.put(offsetXKey, palette.offsetX)
        obj.put(offsetYKey, palette.offsetY)

        val array = JSONArray()

        palette.colorPoints.forEach { entry ->
            array.put(
                JSONObject().
                put(xKey, entry.x).
                put(yKey, entry.y).
                put(colorKey, Colors.toColorString(entry.value.toRgb().toArgb()))
            )
       }

        obj.put(pointsKey, array)

        return obj
    }

    fun toPalette(obj: JSONObject): Palette {
        val width = obj.getInt(widthKey)
        val height = obj.getInt(heightKey)
        val offsetX = obj.getDouble(offsetXKey).toFloat()
        val offsetY = obj.getDouble(offsetYKey).toFloat()

        val points = obj.getJSONArray(pointsKey)
        val colorMap = IntIntMap<Lab>()

        (0 until points.length()).forEach {
            val point = points[it] as JSONObject

            val x = point.get(xKey) as Int
            val y = point.get(yKey) as Int
            val color = Colors.fromColorString(point.get(colorKey).toString())

            colorMap[x, y] = Rgb.of(color).toLab()
        }

        return Palette(width, height, offsetX, offsetY, colorMap)
    }

    private const val widthKey = "width"
    private const val heightKey = "height"
    private const val offsetXKey = "offsetX"
    private const val offsetYKey = "offsetY"
    private const val pointsKey = "points"
    private const val xKey = "x"
    private const val yKey = "y"
    private const val colorKey = "color"
}