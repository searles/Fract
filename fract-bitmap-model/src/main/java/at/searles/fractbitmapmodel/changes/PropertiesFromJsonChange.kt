package at.searles.fractbitmapmodel.changes

import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.FractPropertiesAdapter
import org.json.JSONObject

/**
 * Use this one to load favorites
 */
class PropertiesFromJsonChange(obj: JSONObject): CalcPropertiesChange {

    private val newProperties = FractPropertiesAdapter.fromJson(obj)

    override fun accept(properties: FractProperties): FractProperties {
        return newProperties
    }
}