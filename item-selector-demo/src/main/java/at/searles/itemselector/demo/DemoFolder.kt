package at.searles.itemselector.demo

import android.widget.ImageView
import at.searles.itemselector.model.Folder
import at.searles.itemselector.model.Item

class DemoFolder(override val key: String, override val children: List<Item>): Folder {
    override val title: String = "title: $key saf sa fas f as f asdfasdfasdf asdf as dfa sdf asf" +
            "s af saf asd fas f as f asd fas df as f asfd a g adf sa fsa df asdf as dfas f af"
    override val description: String = "description: $key very long blablablablabld fgdf" +
            "adgd  a sadf asd fas fa sfd asdf asd fa sdf as fd asd f sadf sa df asdf as df asd f sadf as" +
            "sdf saf asf sa fasf"
    override fun setImageInView(imageView: ImageView) {
        // ignore
    }

    override fun toString(): String {
        return title
    }
}