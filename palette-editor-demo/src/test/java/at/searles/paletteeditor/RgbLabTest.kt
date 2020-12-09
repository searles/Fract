package at.searles.paletteeditor

import at.searles.commons.color.Rgb
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RgbLabTest {
    @Test
    fun convertIntToRgb() {
        listOf(
            0xff000000.toInt(),
            0xffff0000.toInt(),
            0xff00ff00.toInt(),
            0xff0000ff.toInt(),
            0xffffff00.toInt(),
            0xff00ffff.toInt(),
            0xffff00ff.toInt()
        ).forEach {
            assertEquals(it, Rgb.of(it).toArgb())
        }
    }

    @Test
    fun convertIntToLab() {
        listOf(
            0xff000000.toInt(),
            0xffff0000.toInt(),
            0xff00ff00.toInt(),
            0xff0000ff.toInt(),
            0xffffff00.toInt(),
            0xff00ffff.toInt(),
            0xffff00ff.toInt()
        ).forEach {
            assertEquals(it, Rgb.of(it).toLab().toRgb().toArgb())
        }
    }
}
