package at.searles.fract

import android.graphics.Typeface
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
    private var items = emptyList<Item>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if(parameterBoolType == viewType) {
            val view = LayoutInflater.from(activity).inflate(R.layout.parameter_bool_item, parent, false)
            val vh = ViewHolder(view)
            view.findViewById<CheckBox>(R.id.parameterNameCheckBox).setOnCheckedChangeListener(vh)
            vh
        } else {
            val view = LayoutInflater.from(activity).inflate(R.layout.parameter_simple_item, parent, false)
            ViewHolder(view).also {
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
            val descriptionTextView = itemView.findViewById<TextView>(R.id.descriptionTextView)

            when(item.type) {
                parameterBoolType -> {
                    val checkBox = itemView.findViewById<CheckBox>(R.id.parameterNameCheckBox)

                    // Must remove listener to avoid a callback
                    checkBox.setOnCheckedChangeListener(null)
                    checkBox.isChecked = item.value as Boolean
                    checkBox.setOnCheckedChangeListener(this)

                    checkBox.text = item.description

                    if(item.isDefault) {
                        checkBox.typeface = Typeface.DEFAULT
                    } else {
                        checkBox.typeface = Typeface.DEFAULT_BOLD
                    }

                    descriptionTextView.text = item.name
                }
                else -> {
                    val textView = itemView.findViewById<TextView>(R.id.parameterNameTextView)
                    textView.text = item.description

                    if(item.isDefault) {
                        textView.typeface = Typeface.DEFAULT
                    } else {
                        textView.typeface = Typeface.DEFAULT_BOLD
                    }

                    descriptionTextView.text = item.name
                }
            }
        }
    }

    fun updateFrom(model: FractBitmapModel) {
        val list = ArrayList<Item>()

        val properties = model.properties

        // TODO Strings!
        list.add(Item(activity.resources.getString(R.string.sourceCode), sourceCodeType, "Source Code", false, properties.sourceCode))
        list.add(Item(activity.resources.getString(R.string.scale), scaleType, "Scale", properties.isDefaultScale, properties.scale))
        list.add(Item(activity.resources.getString(R.string.shaderProperties), shaderPropertiesType, "Light Effects",model.properties.isDefaultShaderProperties, properties.shaderProperties))

        require(list.size == paletteStartPosition)

        repeat(properties.paletteCount) {
            list.add(Item("Palette $it", paletteType, properties.getPaletteDescription(it), properties.isDefaultPalette(it), properties.getPalette(it)))
        }

        properties.parameterIds.forEach {
            val expr = properties.getParameter(it)
            val isDefault = properties.isDefaultParameter(it)
            val description = properties.getParameterDescription(it)

            if(expr == "true" || expr == "false") {
                list.add(Item(it, parameterBoolType, description, isDefault, expr == "true"))
            } else {
                list.add(Item(it, parameterType, description, isDefault, expr))
            }
        }

        items = list
        notifyDataSetChanged()
    }

    class Item(val name: String, val type: Int, val description: String, val isDefault: Boolean, val value: Any)

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