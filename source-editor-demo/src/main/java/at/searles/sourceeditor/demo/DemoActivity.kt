package at.searles.sourceeditor.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import at.searles.sourceeditor.SourceEditorActivity

class DemoActivity : AppCompatActivity() {

    private val runButton: Button by lazy {
        findViewById<Button>(R.id.button)
    }

    lateinit var source: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        source = "val z0 = 0:0;\n" +
                "var c = point;\n" +
                "var n = 0;\n" +
                "\n" +
                "var z = z0;\n" +
                "\n" +
                "val bailoutValue = 64;\n" +
                "val maxExponent = 2;\n" +
                "extern maxIterationCount: \"MaxIterationCount\" = \"1024\";\n" +
                "\n" +
                "while ({\n" +
                "    z = z^2 + c;\n" +
                "    \n" +
                "    var logZ = log z;\n" +
                "    \n" +
                "    if(re logZ > bailoutValue) {\n" +
                "        var continuousAddend = -log(re logZ / log bailoutValue) / log maxExponent;\n" +
                "        var continuousN = n + continuousAddend;\n" +
                "        setResult(1, log (1 + continuousN), continuousN);\n" +
                "        false\n" +
                "    } else if(not next(maxIterationCount, n)) {\n" +
                "        setResult(0, im logZ / 2 pi, re logZ);\n" +
                "        false\n" +
                "    } else {\n" +
                "        true\n" +
                "    }\n" +
                "})\n"

        runButton.setOnClickListener {
            Intent(this, SourceEditorActivity::class.java).also {
                it.putExtra(SourceEditorActivity.sourceKey, source)
                it.putExtra(SourceEditorActivity.parametersKey,
                        SourceEditorActivity.toBundle(mapOf("maxIterationCount" to "123")))

                startActivityForResult(it, sourceRequestCode)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == sourceRequestCode && resultCode == Activity.RESULT_OK) {
            source = data!!.getStringExtra(SourceEditorActivity.sourceKey)!!
            return
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val sourceRequestCode = 261
    }
}
