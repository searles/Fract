package at.searles.fractbitmapprovider

import at.searles.commons.math.Scale
import at.searles.fractbitmapmodel.changes.CalcPropertiesChange
import at.searles.fractbitmapmodel.changes.ScaleChange
import at.searles.fractlang.FractlangProgram
import org.junit.Assert
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CalcChangeTest {
    private lateinit var calcProperties: CalcProperties

    @Test
    fun defaultScaleTest() {
        withSourceCode("")

        Assert.assertEquals(2.0, calcProperties.scale.xx, 0.0)
        Assert.assertEquals(0.0, calcProperties.scale.xy, 0.0)
        Assert.assertEquals(0.0, calcProperties.scale.yx, 0.0)
        Assert.assertEquals(2.0, calcProperties.scale.yy, 0.0)
        Assert.assertEquals(0.0, calcProperties.scale.cx, 0.0)
        Assert.assertEquals(0.0, calcProperties.scale.cy, 0.0)
    }

    @Test
    fun scaleChangeTest() {
        withSourceCode("")

        applyChange(ScaleChange(Scale(4.0, 1.0, 0.0, 3.0, 2.0, 5.0)))

        Assert.assertEquals(4.0, calcProperties.scale.xx, 0.0)
        Assert.assertEquals(1.0, calcProperties.scale.xy, 0.0)
        Assert.assertEquals(0.0, calcProperties.scale.yx, 0.0)
        Assert.assertEquals(3.0, calcProperties.scale.yy, 0.0)
        Assert.assertEquals(2.0, calcProperties.scale.cx, 0.0)
        Assert.assertEquals(5.0, calcProperties.scale.cy, 0.0)
    }

    private fun withSourceCode(sourceCode: String) {
        calcProperties = CalcProperties(null, FractlangProgram(sourceCode, emptyMap()))
    }

    private fun applyChange(change: CalcPropertiesChange) {
        calcProperties = change.accept(calcProperties)
    }
}
