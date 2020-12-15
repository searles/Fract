package at.searles.commons.color

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*

class RybColorsTest {

    @Test
    fun red() {
        Assert.assertEquals(1F, RybColors.red(0F))
        Assert.assertEquals(1F, RybColors.red(0.25F))
        Assert.assertEquals(0F, RybColors.red(0.5F))
        Assert.assertEquals(0.5F, RybColors.red(0.75F))
        Assert.assertEquals(1F, RybColors.red(1F))
    }

    @Test
    fun green() {
        Assert.assertEquals(0F, RybColors.green(0F))
        Assert.assertEquals(0.75F, RybColors.green(0.25F))
        Assert.assertEquals(1F, RybColors.green(0.5F))
        Assert.assertEquals(0F, RybColors.green(0.75F))
        Assert.assertEquals(0F, RybColors.green(1F))
    }

    @Test
    fun blue() {
        Assert.assertEquals(0F, RybColors.blue(0F))
        Assert.assertEquals(0F, RybColors.blue(0.25F))
        Assert.assertEquals(0F, RybColors.blue(0.5F))
        Assert.assertEquals(1F, RybColors.blue(0.75F))
        Assert.assertEquals(0F, RybColors.blue(1F))
    }

    @Test
    fun color() {
        Assert.assertEquals(Rgb(1f, 0f, 0f), RybColors.color(0F))
        Assert.assertEquals(Rgb(0f, 1f, 0f), RybColors.color(0.5F))
        Assert.assertEquals(Rgb(1f, 0f, 0f), RybColors.color(1F))
    }

    @Test
    fun hue() {
        Assert.assertEquals(0F, RybColors.hue(1F, 0.9F, 0.9F))
        Assert.assertEquals(0F, RybColors.hue(0.1F, 0.04F, 0.04F))
    }
}