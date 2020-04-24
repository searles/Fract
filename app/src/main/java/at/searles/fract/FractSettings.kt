package at.searles.fract

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 3 Stages: fallBack, default and current.
 */
@Parcelize
class FractSettings(
    val mode: Mode = FactorySettings.mode,
    val isRotationLock: Boolean = FactorySettings.factoryIsRotationLock,
    val isConfirmZoom: Boolean = FactorySettings.factoryIsConfirmZoom,
    val isGridEnabled: Boolean = FactorySettings.factoryIsGridEnabled,
    val excludeFromPaletteMode: List<String> = emptyList(),
    val width: Int = FactorySettings.factoryWidth,
    val height: Int = FactorySettings.factoryHeight
) : Parcelable {

    fun withMode(mode: Mode): FractSettings {
        return FractSettings(mode, isRotationLock, isConfirmZoom, isGridEnabled, excludeFromPaletteMode, width, height)
    }

    fun withSize(width: Int, height: Int): FractSettings {
        return FractSettings(mode, isRotationLock, isConfirmZoom, isGridEnabled, excludeFromPaletteMode, width, height)
    }

    fun isExcludeFromPaletteMode(label: String, isChecked: Boolean): FractSettings {
        if(isChecked) {
            return FractSettings(
                mode,
                isRotationLock,
                isConfirmZoom,
                isGridEnabled,
                excludeFromPaletteMode + label,
                width,
                height
            )
        } else {
            return FractSettings(
                mode,
                isRotationLock,
                isConfirmZoom,
                isGridEnabled,
                excludeFromPaletteMode - label,
                width,
                height
            )

        }
    }

    enum class Mode { None, Scale, Light, Palette }
}