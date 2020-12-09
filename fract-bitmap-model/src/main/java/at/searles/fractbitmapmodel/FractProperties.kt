package at.searles.fractbitmapmodel

import android.graphics.Matrix
import androidx.core.graphics.values
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fractlang.FractlangProgram

class FractProperties(
    val program: FractlangProgram,
    val customScale: Scale?,
    val customShaderProperties: ShaderProperties?,
    val customPalettes: Map<String, Palette>) {
    val sourceCode: String = program.sourceCode

    val scale = customScale ?: program.defaultScale
    val isDefaultScale = customScale == null

    val shaderProperties = customShaderProperties ?: fallBackShaderProperties
    val isDefaultShaderProperties = customShaderProperties == null

    val vmCode
        get() = program.vmCode

    fun getPalette(label: String): Palette {
        return customPalettes[label] ?: program.defaultPalettes.getValue(label).defaultPalette
    }

    val paletteLabels by lazy {
        program.defaultPalettes.keys.toList().sortedBy {
            program.defaultPalettes.getValue(it).index
        }
    }

    val paletteList by lazy {
        paletteLabels.map { getPalette(it) }
    }

    fun isDefaultPalette(label: String): Boolean {
        return !customPalettes.containsKey(label)
    }

    fun getPaletteDescription(label: String): String {
        return program.defaultPalettes.getValue(label).description
    }

    val parameterIds: Iterable<String> = program.activeParameters.keys

    fun getParameterDescription(id: String): String {
        return program.activeParameters.getValue(id).description
    }

    /**
     * Since FractProgram is compiled using custom parameters,
     * this will be identical to the ones in customParameters.
     */
    fun getParameter(id: String): String {
        return program.activeParameters.getValue(id).expr
    }

    fun isDefaultParameter(id: String): Boolean {
        return program.activeParameters.getValue(id).isDefault
    }

    val customParameters = program.customParameters

    fun createWithNewBitmapProperties(customPalettes: Map<String, Palette>?, customShaderProperties: ShaderProperties?): FractProperties {
        return FractProperties(program,
            customScale,
            customShaderProperties ?: this.customShaderProperties,
            customPalettes ?: this.customPalettes
        )
    }

    fun createWithRelativeScale(relativeMatrix: Matrix): FractProperties {
        val mInverse = Matrix()
        relativeMatrix.invert(mInverse)

        val newScale = scale.createRelative(Scale.fromMatrix(*mInverse.values()))

        return createWithNewScale(newScale)
    }

    fun createWithNewScale(newScale: Scale): FractProperties {
        return FractProperties(program, newScale, customShaderProperties, customPalettes)
    }

    fun createWithNewProperties(newFractlangProgram: FractlangProgram): FractProperties {
        return create(newFractlangProgram, customScale, customShaderProperties, customPalettes)
    }

    companion object {
        fun create(sourceCode: String, customParameters: Map<String, String>, customScale: Scale?, customShaderProperties: ShaderProperties?, customPalettes: Map<String, Palette>): FractProperties {
            val program = FractlangProgram(sourceCode, customParameters)

            return create(program, customScale, customShaderProperties, customPalettes)
        }

        fun create(program: FractlangProgram, customScale: Scale?, customShaderProperties: ShaderProperties?, customPalettes: Map<String, Palette>): FractProperties {
            return FractProperties(program,
                customScale,
                customShaderProperties,
                customPalettes
            )
        }

        private val fallBackShaderProperties = ShaderProperties(true)
    }
}