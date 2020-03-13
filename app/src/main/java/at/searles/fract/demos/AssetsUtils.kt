package at.searles.fract.demos

import android.content.Context
import java.io.BufferedReader

object AssetsUtils {
    fun readAssetSource(context: Context, sourceKey: String): String {
        return context.assets.open("sources/$sourceKey.ft").bufferedReader().use(BufferedReader::readText)
    }

    fun readAssetParameters(context: Context, parametersKey: String): AssetsParameters {
        DemosFolderHolder(context).parametersMap[parametersKey]

        return DemosFolderHolder(context).readParameterFromAssets(parametersKey)
    }
}