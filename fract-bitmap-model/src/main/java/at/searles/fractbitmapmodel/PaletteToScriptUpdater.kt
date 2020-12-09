package at.searles.fractbitmapmodel

import android.renderscript.Matrix4f
import android.renderscript.RenderScript
import at.searles.commons.color.Lab
import at.searles.commons.color.Palette
import at.searles.commons.math.InterpolationMatrix
import at.searles.fractbitmapmodel.palette.SplineSegment
import at.searles.fractbitmapmodel.palette.Yuv
import java.util.*

/**
 * Inside the palette I use yuv squared for the sake of efficiency.
 */
class PaletteToScriptUpdater(private val rs: RenderScript, private val script: ScriptC_bitmap) {

    private lateinit var rsSegments: ScriptField_paletteSegment
    private lateinit var rsPalettes: ScriptField_palette

    fun updatePalettes(properties: FractProperties) {
        val splineSegments = ArrayList<SplineSegment>()

        val palettesWithSegmentIndex = properties.paletteList.map {palette ->
            val segmentStartIndex = splineSegments.size
            val segments = createSplineSegments(palette)

            segments.forEach { splineSegments.addAll(it) }

            Pair(palette, segmentStartIndex)
        }

        script._palettesCount = properties.paletteList.size.toLong()

        setPalettesInScript(palettesWithSegmentIndex)
        setSegmentsInScript(splineSegments)
    }

    fun updateOffsets(index: Int, offsetX: Float, offsetY: Float) {
        script._palettes.set_offsetX(index, offsetX, false)
        script._palettes.set_offsetY(index, offsetY, false)

        script._palettes.copyAll()
    }

    private fun setSegmentsInScript(splineSegments: ArrayList<SplineSegment>) {
        rsSegments = ScriptField_paletteSegment(rs, splineSegments.size)
        script.bind_segments(rsSegments)

        splineSegments.forEachIndexed { index, splineSegment ->
            rsSegments.set_comp0(index, Matrix4f(splineSegment.comp0.flts()), false)
            rsSegments.set_comp1(index, Matrix4f(splineSegment.comp1.flts()), false)
            rsSegments.set_comp2(index, Matrix4f(splineSegment.comp2.flts()), false)
            rsSegments.set_alpha(index, Matrix4f(splineSegment.alpha.flts()), false)
        }

        script._segments.copyAll()
    }

    private fun setPalettesInScript(palettesWithSegmentIndex: List<Pair<Palette, Int>>) {
        rsPalettes = ScriptField_palette(rs, palettesWithSegmentIndex.size)
        script.bind_palettes(rsPalettes)

        palettesWithSegmentIndex.forEachIndexed { index, pair ->
            val palette = pair.first
            val segmentIndex = pair.second

            rsPalettes.set_w(index, palette.width.toLong(), false)
            rsPalettes.set_h(index, palette.height.toLong(), false)

            rsPalettes.set_offsetX(index, palette.offsetX, false)
            rsPalettes.set_offsetY(index, palette.offsetY, false)

            rsPalettes.set_segmentIndex(index, segmentIndex.toLong(), false)
        }

        script._palettes.copyAll()
    }

    private fun toComponents(lab: Lab): FloatArray {
        // XXX If yuv is used, it must be switched here and in bitmap.rs
        //return floatArrayOf(lab.l, lab.a, lab.b, lab.alpha)
        val yuv = Yuv(lab.toRgb())
        return floatArrayOf(yuv.y, yuv.u, yuv.v, yuv.alpha)
    }

    private fun createSplineSegments(palette: Palette): Array<Array<SplineSegment>> {
        val height = palette.height
        val width = palette.width
        val colors = palette.colorTable

        // Create a matrix of all color components

        val components = Array(4) {
            Array(height) {
                DoubleArray(width)
            }
        }

        (0 until height).forEach { y ->
            (0 until width).forEach { x ->
                with(colors[y][x]) {
                    val color = toComponents(this)
                    repeat(4) {
                        components[it][y][x] = color[it].toDouble()
                    }
                }
            }
        }

        val splines = Array(4) {
            InterpolationMatrix.create(components[it])
        }

        return Array(height) {y ->
            Array(width) { x ->
                SplineSegment(
                    splines[0][y][x],
                    splines[1][y][x],
                    splines[2][y][x],
                    splines[3][y][x]
                )
            }
        }
    }
}