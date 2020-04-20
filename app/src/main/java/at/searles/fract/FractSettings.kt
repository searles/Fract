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
    val excludeFromPaletteEdit: List<String> = emptyList()
) : Parcelable {
    enum class Mode { None, Scale, Light, Palette }
}