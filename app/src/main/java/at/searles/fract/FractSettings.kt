package at.searles.fract

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * 3 Stages: fallBack, default and current.
 */
@Parcelize
class FractSettings(
    val isRotationLock: Boolean = FactorySettings.factoryIsRotationLock,
    val isTouchEnabled: Boolean = FactorySettings.factoryIsTouchEnabled,
    val isConfirmZoom: Boolean = FactorySettings.factoryIsConfirmZoom,
    val isGridEnabled: Boolean = FactorySettings.factoryIsGridEnabled,
    val isEditLightsOnScreenEnabled: Boolean = FactorySettings.factoryIsEditLightsOnScreenEnabled
) : Parcelable