package at.searles.fract

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import at.searles.fractbitmapmodel.FractBitmapModel

class ParameterAdapter(private val activity: FractMainActivity): RecyclerView.Adapter<ParameterAdapter.ViewHolder>() {

    // Order: Scale, SourceCode, ShaderProperties, Palettes[n], Parameters.
    private var items = emptyList<Item>() // TODO: Can use a listadapter.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if(parameterBoolType == viewType) {
            val view = LayoutInflater.from(activity).inflate(R.layout.parameter_bool_item, parent, false)
            val vh = ViewHolder(view)
            view.findViewById<CheckBox>(R.id.parameterNameCheckBox).setOnCheckedChangeListener(vh)
            return vh
        } else {
            val view = LayoutInflater.from(activity).inflate(R.layout.parameter_simple_item, parent, false)
            return ViewHolder(view).also {
                view.setOnClickListener(it)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].type
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindTo(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        override fun onClick(v: View) {
            val item = items[adapterPosition]

            when(item.type) {
                parameterType -> activity.openParameterEditor(item.name)
                paletteType -> activity.openPaletteEditor(adapterPosition - paletteStartPosition)
                sourceCodeType -> activity.openSourceEditor()
                scaleType -> activity.openScaleEditor()
                shaderPropertiesType -> activity.openShaderPropertiesEditor()
                else -> error("unknown type: ${item.type}")
            }
        }

        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            val item = items[adapterPosition]
            require(item.type == parameterBoolType)
            activity.setParameter(item.name, isChecked.toString())
        }

        internal fun bindTo(item: Item) {
            when(item.type) {
                parameterBoolType -> {
                    val checkBox = itemView.findViewById<CheckBox>(R.id.parameterNameCheckBox)
                    checkBox.isChecked = item.value as Boolean
                    checkBox.text = item.name
                }
                else -> {
                    val textView = itemView.findViewById<TextView>(R.id.parameterNameTextView)
                    textView.text = item.name
                }
            }
        }
    }

    fun updateFrom(model: FractBitmapModel) {
        val list = ArrayList<Item>()

        list.add(Item(activity.resources.getString(R.string.sourceCode), sourceCodeType, model.sourceCode))
        list.add(Item(activity.resources.getString(R.string.scale), scaleType, model.scale))
        list.add(Item(activity.resources.getString(R.string.shaderProperties), shaderPropertiesType, model.shaderProperties))

        require(list.size == paletteStartPosition)
        model.palettes.forEachIndexed {
                index, palette -> list.add(Item("Palette $index", paletteType, palette))
        }

        model.parameters.forEach { (key, value) ->
            if(value == "true" || value == "false") {
                list.add(Item(key, parameterBoolType, value == "true"))
            } else {
                list.add(Item(key, parameterType, value))
            }
        }

        items = list
        notifyDataSetChanged()
    }

    class Item(val name: String, val type: Int, val value: Any)

    companion object {
        private const val sourceCodeType = 0
        private const val scaleType = 1
        private const val shaderPropertiesType = 2
        private const val paletteType = 3
        private const val parameterType = 4
        private const val parameterBoolType = 5

        private const val paletteStartPosition: Int = 3
    }
}