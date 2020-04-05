package at.searles.fract.demos

import android.content.Context
import at.searles.fract.experimental.BulkCalculator
import at.searles.fractbitmapmodel.FractProperties
import at.searles.itemselector.model.Item

class AssetBulkIconGenerator(private val context: Context): Iterable<FractProperties> {
    override fun iterator(): Iterator<FractProperties> {
        val folderHolder = DemosFolderHolder(context)

        val folderIterator = folderHolder.folders.iterator()

        return object: Iterator<FractProperties> {

            lateinit var folder: AssetsSourceFolder
            lateinit var sourceCode: String
            var parameterIterator: Iterator<Item>? = null

            override fun hasNext(): Boolean {
                return folderIterator.hasNext() || parameterIterator != null && parameterIterator!!.hasNext()
            }

            override fun next(): FractProperties {
                if(parameterIterator == null || !parameterIterator!!.hasNext()) {
                    folder = folderIterator.next() as AssetsSourceFolder
                    parameterIterator = folder.children.iterator()
                    sourceCode = AssetsUtils.readAssetSource(context, folder.key)
                }

                val item = parameterIterator!!.next() as AssetsParametersItem

                val parameters = item.parameters.toMutableMap().apply {
                    put(BulkCalculator.filenameKey, "${folder.key}-${item.key}")
                }

                return FractProperties.create(sourceCode, parameters, item.scale, null, emptyList())
            }
        }
    }
}