package at.searles.itemselector.demo

import android.content.Context
import at.searles.itemselector.model.Folder
import at.searles.itemselector.model.FoldersHolder

class DemoInitializer(context: Context): FoldersHolder {
    override val folders: List<Folder> = listOf(
        DemoFolder(
            "A",
            listOf(
                DemoItem("a"),
                DemoItem("b")
            )
        ),
        DemoFolder(
            "B",
            listOf(
                DemoItem("a"),
                DemoItem("b"),
                DemoItem("c")
            )
        ),
        DemoFolder("C", listOf(DemoItem("a"))),
        DemoFolder(
            "D",
            listOf(
                DemoItem("a"),
                DemoItem("b")
            )
        )
    )
}