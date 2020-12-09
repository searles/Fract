package at.searles.fractbitmapprovider.demo

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import at.searles.fractbitmapmodel.BitmapController
import at.searles.fractbitmapmodel.FractBitmapModel
import at.searles.fractbitmapmodel.FractBitmapModelFragment
import at.searles.fractbitmapmodel.FractProperties
import at.searles.fractbitmapmodel.changes.PaletteOffsetChange
import at.searles.fractimageview.ScalableImageView
import at.searles.fractlang.FractlangProgram

class DemoActivity : AppCompatActivity(), BitmapController.Listener, FractBitmapModel.Listener {

    private val imageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.scalableImageView)
    }

    private lateinit var bitmapModelFragment: FractBitmapModelFragment

    private val experimentButton by lazy {
        findViewById<Button>(R.id.triggerExperimentButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView.visibility = View.INVISIBLE

        initBitmapModelFragment()

        experimentButton.setOnClickListener {
            bitmapModelFragment.bitmapModel.stopAnimation()
        }
    }

    override fun onResume() {
        super.onResume()
        connectBitmapModelFragment()
    }

    private fun initBitmapModelFragment() {
        val fragment =
            supportFragmentManager.findFragmentByTag(bitmapModelFragmentTag) as FractBitmapModelFragment?

        bitmapModelFragment = fragment ?:
                FractBitmapModelFragment.createInstance(defaultProperties, 1280, 720).also {
                    supportFragmentManager.beginTransaction().add(it, bitmapModelFragmentTag).commit()
                }
    }

    private val defaultProperties =
        FractProperties(FractlangProgram(sourceCode, emptyMap()), null, null, emptyMap())

    var cycleTask: Runnable? = null

    private fun connectBitmapModelFragment() {
        imageView.bitmapModel = bitmapModelFragment.bitmapModel
        imageView.visibility = View.VISIBLE
        imageView.invalidate()

        bitmapModelFragment.bitmapModel.listener = this

        startColorCycling()
    }

    private fun startColorCycling() {
        if(cycleTask != null) {
            return
        }

        val handler = Handler()
     //   bitmapModelFragment.bitmapModel.startAnimation(720)
       // cycleTask = ColorCycling(handler, 10)
//        cycleTask!!.run()
    }

    inner class ColorCycling(private val handler: Handler, private val delay: Long): Runnable {
        private val startTime = System.currentTimeMillis()

        override fun run() {
            val currentTime = System.currentTimeMillis()

            val offsetX = (currentTime - startTime) / 4000f
            val offsetY = 0f

            val paletteLabel = defaultProperties.paletteLabels[0]

            val change = PaletteOffsetChange(paletteLabel, offsetX, offsetY)

            bitmapModelFragment.bitmapModel.applyBitmapPropertiesChange(change)

            handler.postDelayed(this, delay)
        }
    }


    override fun started() {
        Log.d("DemoActivity", "started")
    }

    override fun setProgress(progress: Float) {
        // Log.d("DemoActivity", "progress: $progress")
    }

    override fun bitmapUpdated() {
        Log.d("DemoActivity.bitmapUpdated", "invalidate image view")
        imageView.invalidate()
    }

    override fun finished() {
        Log.d("DemoActivity", "finished")
        imageView.invalidate()
    }

    override fun propertiesChanged(src: FractBitmapModel) {
        // ignore.
    }

    companion object {
        const val bitmapModelFragmentTag = "bitmapModelFragment"
        const val sourceCode = """
//            var paletteIndex = putPalette("Palette1", "1", 4, 1, [0,0,#ffff0000], [1,0,#ffffff00], [2,0,#ff00ff00], [3,0,#ff0000ff]);
//            setResult(paletteIndex, rect(-0.5:-0.5, 0.5:0.5, point), 0);
//            declareScale(1,0,0,1,0,0);
// Labels of of extern parameters
val maxIterationLabel = "Maximum Iteration Count";
val functionLabel = "Function (z[n])";
val maxExponentLabel = "Maximum Exponent";
val isJuliaSetLabel = "Julia Set";
val juliaSetParameterLabel = "Julia Set Parameter";
val z0Label = "Start Value (z[0])";
val isBoundLabel = "All Points are bound";
val bailoutRadiusLabel = "Bailout Radius";
val bailoutValueLabel = "Bailout Value";
val bailoutColorValueLabel = "Bailout Color Value";
val bailoutHeightLabel = "Bailout Height";
val bailoutPaletteLabel = "Bailout Palette";
val bailoutPaletteDescription = "Palette for Unbound Points";
val epsilonRadiusLabel = "Epsilon Radius";
val lakeValueLabel = "Lake Value";
val lakeColorValueLabel = "Lake Color Value";
val lakeHeightLabel = "Lake Height";
val lakePaletteLabel = "Lake Palette";
val lakePaletteDescription = "Palette for Bound Points";

setScale(2, 0, 0, 2, 0, 0); // Default Scale

class Calculator {
    extern maxIteration: maxIterationLabel = "250";

    var n: Int = 0; // count iterations
    var c: Cplx;    // parameter
    var z: Cplx;
    var lastZ: Cplx = 0:0; // z[n-1]
    var nextZ: Cplx; // z[n+1]
    var radZ: Real; // |z[n]|
    var dz: Cplx;   // z[n+1] - z[n]
    var radDz: Real; //  |dz|

    fun init() {
        extern isJuliaSet: isJuliaSetLabel = "false";
        c = if(isJuliaSet) {
            extern juliaSetParameter: juliaSetParameterLabel = "-0.75: 0.25";
            juliaSetParameter
        } else {
            point;
        }

        z = if(isJuliaSet) {
            point
        } else {
            extern z0: z0Label = "0";
            z0;
        }
    }

    extern isBound: isBoundLabel = "false";

    /*
    * Performs one full calculation step
    * z, lastZ and n are modified.
    * true is returned if none of the break conditions succeeds.
    */
    fun step() {
        extern function: functionLabel = "z^2 + c";
        nextZ = function;

        radZ = rad nextZ;
        dz = nextZ - z;
        radDz = rad dz;
    }

    fun advance() {
        lastZ = z;
        z = nextZ;
    }

    fun setLakeValues() {
        val lakePaletteIndex = if(not isBound) {
            putPalette(lakePaletteLabel, lakePaletteDescription, 1, 1, [0, 0, #ff000000])
        } else {
            putPalette(lakePaletteLabel, lakePaletteDescription, 2, 2,
                [1, 0, #ff000000], [0, 0, #ff4400aa], [1, 1, #ffffdd22], [0, 1, #ffffffff]);
        };

        extern lakeValue: lakeValueLabel = "log1p rad z : argnorm z";
        extern lakeColorValue: lakeColorValueLabel = "value";
        extern lakeHeight: lakeHeightLabel = "re value";

        var value = lakeValue;
        setResult(lakePaletteIndex, lakeColorValue, lakeHeight);
    }

    fun isMaxIteration() {
        if(not next(maxIteration, n)) {
            setLakeValues();
            true
        } else {
            false
        }
    }

    fun isEpsilon() {
        extern epsilonRadius: epsilonRadiusLabel = "1e-9";

        if(radDz < epsilonRadius) {
            setLakeValues();
            true
        } else {
            false
        }
    }

    fun setBailoutValues() {
        val paletteIndex = putPalette(bailoutPaletteLabel, bailoutPaletteDescription, 6, 1,
            [0, 0, #ff000000], [1, 0, #ff4400aa], [5, 0, #ff4400aa], [2, 0, #ffffdd22], [4, 0, #ffffdd22], [3, 0, #ffffffff]);

        extern maxExponent: maxExponentLabel = "2";

        // smoothness value for fractal polynoms
        var continuousAddend =
            1 - log(log radZ / log bailoutRadius) / log maxExponent;

        extern bailoutValue: bailoutValueLabel = "log(n + continuousAddend + 20.08)";
        extern bailoutColorValue: bailoutColorValueLabel = "value";
        extern bailoutHeight: bailoutHeightLabel = "re value";

        var value = bailoutValue;
        setResult(paletteIndex, bailoutColorValue, bailoutHeight);
    }

    fun isBailout() {
        extern bailoutRadius: bailoutRadiusLabel = "64";

        if(radZ >= bailoutRadius) {
            setBailoutValues();
            true
        } else {
            false
        }
    }

    fun isBreak() {
        (if(not isBound) isBailout() else false) or
        isEpsilon() or
        isMaxIteration()
    }
}

val calculator = Calculator();

calculator.init();

while ({
    calculator.step();
    not calculator.isBreak()
}) {
    calculator.advance();
}

            """
    }
}
