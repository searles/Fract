package at.searles.fract

import android.os.Parcelable
import at.searles.commons.math.Cplx
import kotlinx.android.parcel.Parcelize

/**
 * 3 Stages: fallBack, default and current.
 */
@Parcelize
class FractSettings(
    val mode: Mode = FactorySettings.mode,
    val isRotationLock: Boolean = FactorySettings.factoryIsRotationLock,
    val isCenterLock: Boolean = FactorySettings.factoryIsCenterLock,
    val isConfirmZoom: Boolean = FactorySettings.factoryIsConfirmZoom,
    val isGridEnabled: Boolean = FactorySettings.factoryIsGridEnabled,
    val excludedFromPaletteList: List<String> = emptyList(),
    val orbitStartPoint: DoubleArray? = null,
    val width: Int = FactorySettings.factoryWidth,
    val height: Int = FactorySettings.factoryHeight
) : Parcelable {

    fun withMode(mode: Mode): FractSettings {
        return FractSettings(mode, isRotationLock, isCenterLock, isConfirmZoom, isGridEnabled, excludedFromPaletteList, orbitStartPoint, width, height)
    }

    fun withSize(width: Int, height: Int): FractSettings {
        return FractSettings(mode, isRotationLock, isCenterLock, isConfirmZoom, isGridEnabled, excludedFromPaletteList, orbitStartPoint, width, height)
    }

    fun isExcludeFromPaletteMode(label: String, isChecked: Boolean): FractSettings {
        if(isChecked) {
            return FractSettings(
                mode,
                isRotationLock,
                isCenterLock,
                isConfirmZoom,
                isGridEnabled,
                excludedFromPaletteList + label,
                orbitStartPoint,
                width,
                height
            )
        } else {
            return FractSettings(
                mode,
                isRotationLock,
                isCenterLock,
                isConfirmZoom,
                isGridEnabled,
                excludedFromPaletteList - label,
                orbitStartPoint,
                width,
                height
            )
        }
    }

    fun withOrbitStartPoint(value: Cplx?): FractSettings {
        return FractSettings(
            mode,
            isRotationLock,
            isCenterLock,
            isConfirmZoom,
            isGridEnabled,
            excludedFromPaletteList,
            value?.let { doubleArrayOf(it.re(), it.im()) },
            width,
            height
        )
    }

    enum class Mode { None, Scale, Light, Palette, Orbit }
}