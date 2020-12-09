package at.searles.fractbitmapmodel

import android.os.Bundle
import android.renderscript.Float3
import org.json.JSONObject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class ShaderProperties(
    val useLightEffect: Boolean = true,
    val polarAngle: Double = PI / 4.0,
    val azimuthAngle: Double = 3.0 * PI / 4.0,
    val ambientReflection: Float = 0.67f,
    val diffuseReflection: Float = 0.33f,
    val specularReflection: Float = 1f,
    val shininess: Int = 64) {

    val lightVector: Float3
        get() = getLightVector(polarAngle, azimuthAngle)

    fun createWithNewLightVector(newPolarAngle: Double, newAzimuthAngle: Double): ShaderProperties {
        return ShaderProperties(useLightEffect, newPolarAngle, newAzimuthAngle,
            ambientReflection, diffuseReflection, specularReflection, shininess
        )
    }

    fun toJson(): JSONObject {
        val obj = JSONObject()
        obj.put(useLightEffectKey, useLightEffect)

        if(useLightEffect) {
            obj.put(polarAngleKey, polarAngle)
            obj.put(azimuthAngleKey, azimuthAngle)
            obj.put(ambientReflectionKey, ambientReflection)
            obj.put(diffuseReflectionKey, diffuseReflection)
            obj.put(specularReflectionKey, specularReflection)
            obj.put(shininessKey, shininess)
        }

        return obj
    }

    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putBoolean(useLightEffectKey, useLightEffect)

        bundle.putDouble(polarAngleKey, polarAngle)
        bundle.putDouble(azimuthAngleKey, azimuthAngle)
        bundle.putFloat(ambientReflectionKey, ambientReflection)
        bundle.putFloat(diffuseReflectionKey, diffuseReflection)
        bundle.putFloat(specularReflectionKey, specularReflection)
        bundle.putInt(shininessKey, shininess)

        return bundle
    }

    companion object {
        fun fromJson(obj: JSONObject): ShaderProperties {
            val useLightEffect = obj.getBoolean(useLightEffectKey)

            if(!useLightEffect) {
                return ShaderProperties(false)
            }

            val polarAngle = obj.getDouble(polarAngleKey)
            val azimuthAngle = obj.getDouble(azimuthAngleKey)
            val ambientReflection = obj.getDouble(ambientReflectionKey).toFloat()
            val diffuseReflection = obj.getDouble(diffuseReflectionKey).toFloat()
            val specularReflection = obj.getDouble(specularReflectionKey).toFloat()
            val shininess = obj.getInt(shininessKey)

            return ShaderProperties(useLightEffect, polarAngle, azimuthAngle, ambientReflection, diffuseReflection, specularReflection, shininess)
        }

        fun fromBundle(bundle: Bundle): ShaderProperties {
            return ShaderProperties(
                bundle.getBoolean(useLightEffectKey),
                bundle.getDouble(polarAngleKey),
                bundle.getDouble(azimuthAngleKey),
                bundle.getFloat(ambientReflectionKey),
                bundle.getFloat(diffuseReflectionKey),
                bundle.getFloat(specularReflectionKey),
                bundle.getInt(shininessKey)
            )
        }

        fun getLightVector(polarAngle: Double, azimuthAngle: Double): Float3 {
            return Float3(
                (sin(polarAngle) * cos(azimuthAngle)).toFloat(),
                -(sin(polarAngle) * sin(azimuthAngle)).toFloat(),
                cos(polarAngle).toFloat()
            )
        }

        const val useLightEffectKey = "useLightEffect"
        const val polarAngleKey = "polarAngle"
        const val azimuthAngleKey = "azimuthAngle"
        const val ambientReflectionKey = "ambientReflection"
        const val diffuseReflectionKey = "diffuseReflection"
        const val specularReflectionKey = "specularReflection"
        const val shininessKey = "shininess"
    }
}