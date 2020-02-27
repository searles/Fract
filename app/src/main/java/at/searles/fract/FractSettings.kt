package at.searles.fract

import android.content.Context
import android.os.Parcelable
import at.searles.fract.demos.AssetsUtils
import at.searles.fractbitmapmodel.FractProperties
import kotlinx.android.parcel.Parcelize

/**
 * 3 Stages: fallBack, default and current.
 */
@Parcelize
class FractSettings(
    val isRotationLock: Boolean = FactorySettings.factoryIsRotationLock,
    val isTouchEnabled: Boolean = FactorySettings.factoryIsTouchEnabled, // TODO: indicate this on-screen
    val isConfirmZoom: Boolean = FactorySettings.factoryIsConfirmZoom,
    val isGridEnabled: Boolean = FactorySettings.factoryIsGridEnabled
) : Parcelable