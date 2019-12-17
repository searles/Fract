package at.searles.fract.demos

import android.content.Context
import android.net.Uri
import at.searles.itemselector.model.Folder
import at.searles.itemselector.model.FoldersHolder
import org.json.JSONObject

class DemosFoldersHolder(private val context: Context): FoldersHolder {

    private val parametersMap = readParametersFromAssets()

    override val folders: List<Folder> = readSourcesFromAssets()

    init {

    }

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
        val iconFilename = "$id.png"
        val sourceFilename = "$id.ft"
        val name = jsonObject.getString(nameKey)
        val description = jsonObject.getString(descriptionKey)


        val parametersArray = jsonObject.getJSONArray(parametersKey)

        val parameters = (0 until parametersArray.length()).map {
            parametersMap.getValue(parametersArray.getString(it))
        }.toList()

        return AssetsSourceFolder(name, description, iconFilename, sourceFilename, parameters)

    }

    private fun readParametersFromAssets(): Map<String, AssetsParametersItem> {
        val root = readJson(parametersJsonFile)

        val parametersMap = LinkedHashMap<String, AssetsParametersItem>()

        root.keys().forEach { id ->
            parametersMap[id] = createParameterSetFromJson(id, root.getJSONObject(id))
        }

        return parametersMap
    }

    private fun createParameterSetFromJson(id: String, jsonObject: JSONObject): AssetsParametersItem {
        val iconFilename = "$id.png"

        val name = jsonObject.getString(nameKey)
        val description = jsonObject.getString(descriptionKey)

        val parameters = HashMap<String, String>()

        val parametersJson = jsonObject.getJSONObject(parametersKey)

        parametersJson.keys().forEach {
            parameters[it] = parametersJson.getString(it)
        }

        return AssetsParametersItem(name, description, iconFilename, parameters)
    }

    companion object {
        const val sourcesJsonFile = "sources.json"
        const val parametersJsonFile = "parameters.json"

        const val nameKey = "name"
        const val descriptionKey = "description"
        const val parametersKey = "parameters"

        fun getIconUri(iconFilename: String): Uri {
            return Uri.parse("file:///android_asset/icons/$iconFilename")
        }
    }
}