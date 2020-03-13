package at.searles.fract.demos

import android.content.Context
import android.net.Uri
import at.searles.fractbitmapmodel.FractPropertiesAdapter
import at.searles.itemselector.model.Folder
import at.searles.itemselector.model.FoldersHolder
import org.json.JSONObject

class DemosFolderHolder(private val context: Context): FoldersHolder {

    val parametersMap = readParametersFromAssets()
    override val folders: List<Folder> = readSourcesFromAssets()

    private fun readJson(filename: String): JSONObject {
        return context.assets.open(filename).bufferedReader().use {
            JSONObject(it.readText())
        }
    }

    private fun readSourcesFromAssets(): List<AssetsSourceFolder> {
        val root = readJson(sourcesJsonFile)

        val sources = ArrayList<AssetsSourceFolder>()

        root.keys().forEach { id ->
            sources.add(createSourcesFolderFromJson(id, root.getJSONObject(id)))
        }

        return sources
    }

    private fun createSourcesFolderFromJson(id: String, jsonObject: JSONObject): AssetsSourceFolder {
        val iconFilename = "$id-default.png"
        val sourceFilename = "$id.ft"
        val title = jsonObject.getString(titleKey)
        val description = jsonObject.getString(descriptionKey)

        val tags = jsonObject.getString(tagsKey).split(",").toSet()

        val parameterSets = parametersMap.values.filter { parameterSet ->
            tags.any { parameterSet.tags.contains(it) }
        }.map { it.createItem(id) }

        return AssetsSourceFolder(id, title, description, iconFilename, sourceFilename, parameterSets)
    }

    fun readParameterFromAssets(id: String): AssetsParameters {
        val root = readJson(parametersJsonFile)
        return createParameterSetFromJson(id, root.getJSONObject(id))
    }

    private fun readParametersFromAssets(): Map<String, AssetsParameters> {
        val root = readJson(parametersJsonFile)

        val parametersMap = LinkedHashMap<String, AssetsParameters>()

        root.keys().forEach { id ->
            parametersMap[id] = createParameterSetFromJson(id, root.getJSONObject(id))
        }

        return parametersMap
    }

    private fun createParameterSetFromJson(id: String, jsonObject: JSONObject): AssetsParameters {
        val name = jsonObject.getString(titleKey)
        val description = jsonObject.getString(descriptionKey)
        val tags = jsonObject.getString(tagsKey).split(",").toSet()

        val scale = if(jsonObject.has(scaleKey)) {
            FractPropertiesAdapter.scaleFromJson(jsonObject.getJSONArray(scaleKey))
        } else {
            null
        }
        val parameters = HashMap<String, String>()

        val parametersJson = jsonObject.getJSONObject(parametersKey)

        parametersJson.keys().forEach {
            parameters[it] = parametersJson.getString(it)
        }

        return AssetsParameters(id, name, description, tags, scale, parameters)
    }

    companion object {
        const val sourcesJsonFile = "sources.json"
        const val parametersJsonFile = "parameters.json"

        const val titleKey = "title"
        const val descriptionKey = "description"
        const val scaleKey = "scale"
        const val parametersKey = "parameters"
        const val tagsKey = "tags"

        fun getIconUri(iconFilename: String): Uri {
            return Uri.parse("file:///android_asset/icons/$iconFilename")
        }
    }
}