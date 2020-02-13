package at.searles.fract

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.searles.android.storage.StorageActivity
import at.searles.android.storage.data.InvalidNameException
import at.searles.android.storage.data.PathContentProvider
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fract.demos.AssetsUtils
import at.searles.fract.demos.DemosFolderHolder
import at.searles.fract.editors.*
import at.searles.fract.favorites.AddToFavoritesDialogFragment
import at.searles.fract.favorites.FavoritesProvider
import at.searles.fractbitmapmodel.*
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.DrawBitmapBoundsPlugin
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.itemselector.ItemSelectorActivity
import at.searles.paletteeditor.PaletteAdapter
import at.searles.paletteeditor.PaletteEditorActivity
import at.searles.sourceeditor.SourceEditorActivity
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class FractMainActivity : AppCompatActivity(), FractBitmapModel.Listener {

    private lateinit var parameterAdapter: ParameterAdapter
    private lateinit var bitmapModelFragment: FractBitmapModelFragment
    private lateinit var bitmapModel: FractBitmapModel

    private val menuNavigationView: NavigationView by lazy {
        findViewById<NavigationView>(R.id.menuNavigationView)
    }

    private val drawerLayout: DrawerLayout by lazy {
        findViewById<DrawerLayout>(R.id.drawerLayout)
    }

    private val parameterRecyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.parameterRecyclerView)
    }

    private val mainImageView: PluginScalableImageView by lazy {
        findViewById<PluginScalableImageView>(R.id.mainImageView)
    }

    private val taskProgressBar: ProgressBar by lazy {
        findViewById<ProgressBar>(R.id.taskProgressBar).apply {
            isIndeterminate = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        menuNavigationView.setNavigationItemSelectedListener {
            openMainMenuItem(it)
            true
        }

        initBitmapModelFragment()

        mainImageView.visibility = View.INVISIBLE
        taskProgressBar.visibility = View.VISIBLE

        parameterAdapter = ParameterAdapter(this)

        with(parameterRecyclerView) {
            adapter = parameterAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onResume() {
        super.onResume()
        connectBitmapModelFragment()
        taskProgressBar.visibility = View.INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            demoSelectorRequestCode -> {
                require(intent != null)
                val sourceId = intent.getStringExtra(ItemSelectorActivity.folderKey)!!
                val parameterId = intent.getStringExtra(ItemSelectorActivity.itemKey)!!
                val merge = intent.getBooleanExtra(ItemSelectorActivity.specialKey, false)

                loadDemo(sourceId, parameterId, merge)
            }
            saveImageCode -> {
                require(intent != null)
                saveImage(contentResolver.openOutputStream(intent.data!!)!!)
            }
            sourceRequestCode -> {
                val sourceCode = intent!!.getStringExtra(SourceEditorActivity.sourceKey)!!
                val parameters = SourceEditorActivity.toStringMap(intent.getBundleExtra(SourceEditorActivity.parametersKey)!!)

                setSourceCode(sourceCode, parameters)
                return
            }
            paletteRequestCode -> {
                val palette = PaletteAdapter.toPalette(intent!!.getBundleExtra(PaletteEditorActivity.paletteKey)!!)
                val index = intent.getIntExtra(paletteIndexKey, -1)

                setPalette(index, palette)
                return
            }
            favoritesRequestCode -> {
                val favoriteKey = intent!!.getStringExtra(StorageActivity.nameKey)!!
                loadFavorite(favoriteKey)
                return
            }
        }

        super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun loadFavorite(favoriteKey: String) {
        try {
            FavoritesProvider(this).load(favoriteKey) {
                val obj = JSONObject(it)
                bitmapModel.scheduleCalcPropertiesChange(PropertiesFromJsonChange(obj))
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // TODO String extract!
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setPalette(index: Int, palette: Palette) {
        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val palettes = properties.customPalettes.toMutableList()

                while(palettes.size <= index) {
                    palettes.add(null)
                }

                palettes[index] = palette

                return properties.createWithNewBitmapProperties(palettes, null)
            }
        }

        bitmapModel.applyBitmapPropertiesChange(change)
    }

    private fun openMainMenuItem(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.demoAction -> {
                openDemoActivity()
            }
            R.id.openFavoritesAction -> {
                openFavorites()
            }
            R.id.addToFavoritesAction -> {
                openAddToFavoritesDialog()
            }
            R.id.shareAction -> {
                openShareImage()
            }
            R.id.saveAction -> {
                openSaveImage()
            }
            R.id.imageSize -> {
                openImageSize()
            }
            R.id.openSettingsAction -> {
                openSettings()
            }
        }

        drawerLayout.closeDrawers()
    }

    private fun openSettings() {
        SettingsDialogFragment.newInstance(
            mainImageView.isTouchEnabled,
                mainImageView.hasRotationLock,
                mainImageView.mustConfirmZoom
        ).show(supportFragmentManager, "dialog")
    }

    private fun openImageSize() {
        ImageSizeDialogFragment.newInstance(
            bitmapModel.bitmap.width,
            bitmapModel.bitmap.height).show(supportFragmentManager, "dialog")
    }

    private fun saveImage(os: OutputStream) {
        os.use {
            if (!bitmapModel.bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) {
                throw UnsupportedOperationException("compress not supported!")
            }
        }
    }

    private fun openShareImage() {
        val outFile = File.createTempFile(
            "fract_${System.currentTimeMillis()}",
            ".png",
            externalCacheDir
        )

        saveImage(FileOutputStream(outFile))

        val contentUri = FileProvider.getUriForFile(this, FILE_PROVIDER, outFile)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, contentUri)
            type = PathContentProvider.mimeType
        }

        startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.shareImage)))
    }

    private fun openSaveImage() {
        startActivityForResult(
            Intent().apply {
                action = Intent.ACTION_CREATE_DOCUMENT
                addCategory(Intent.CATEGORY_OPENABLE)
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                type = pngMimeType
            },
            saveImageCode
        )
    }

    private fun openAddToFavoritesDialog() {
        AddToFavoritesDialogFragment.newInstance().show(supportFragmentManager, "dialog")
    }

    private fun openFavorites() {
        Intent(this, StorageActivity::class.java).also {
            it.putExtra(StorageActivity.titleKey, resources.getString(R.string.openFavorites))
            it.putExtra(StorageActivity.providerClassNameKey, FavoritesProvider::class.java.canonicalName)
            startActivityForResult(it, favoritesRequestCode)
        }
    }

    private fun loadDemo(sourceKey: String, parametersKey: String, mergeParameters: Boolean) {
        val sourceCode = AssetsUtils.readAssetSource(this, sourceKey)
        val parameters = HashMap<String, String>()

        val currentProperties = bitmapModel.properties

        val scale: Scale?
        val palettes: List<Palette?>

        if(mergeParameters) {
            Toast.makeText(this, "Merging sample with current parameters", Toast.LENGTH_SHORT).show()
            parameters.putAll(currentProperties.customParameters)

            scale = if(!currentProperties.isDefaultScale) currentProperties.scale else null
            palettes = (0 until currentProperties.paletteCount).map {
                if(currentProperties.isDefaultPalette(it)) null else currentProperties.getPalette(it)
            }
        } else {
            scale = null
            palettes = emptyList()
        }

        parameters.putAll(AssetsUtils.readAssetParameters(this, parametersKey))

        try {
            // TODO check
            val properties = FractProperties.create(sourceCode, parameters, scale, currentProperties.shaderProperties, palettes)
            val change = NewFractPropertiesChange(properties)

            bitmapModel.scheduleCalcPropertiesChange(change)
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun setSourceCode(sourceCode: String, parameters: Map<String, String>) {
        try {
            val currentProperties = bitmapModel.properties

            val newProgram = FractlangProgram(sourceCode, parameters)

            val properties = currentProperties.createWithNewProperties(newProgram)
            val change = NewFractPropertiesChange(properties)

            bitmapModel.scheduleCalcPropertiesChange(change)
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun openDemoActivity() {
        Intent(this, ItemSelectorActivity::class.java).also {
            it.putExtra(ItemSelectorActivity.initializerClassNameKey, DemosFolderHolder::class.java.canonicalName)
            startActivityForResult(it, demoSelectorRequestCode)
        }
    }

    fun addToFavorites(name: String) {
        // Save everything except for resolution
        val jsonProperties = FractPropertiesAdapter.toJson(bitmapModel.properties)
        val jsonString = jsonProperties.toString(4)

        try {
            FavoritesProvider(this).save(name, { jsonString }, false)
        } catch(e: InvalidNameException) {
            Toast.makeText(this, getString(R.string.invalidName), Toast.LENGTH_LONG).show()
        }
    }

    private fun createNewBitmapModelFragment(): FractBitmapModelFragment {
        // use default source
        val sourceCode = AssetsUtils.readAssetSource(this, "mandelbrot")

        return FractBitmapModelFragment.createInstance(sourceCode).apply {
            supportFragmentManager.beginTransaction().add(this, fractBitmapModelFragmentTag).commit()
        }
    }

    private fun initBitmapModelFragment() {
        val fragment = supportFragmentManager.findFragmentByTag(fractBitmapModelFragmentTag)
        bitmapModelFragment = fragment as FractBitmapModelFragment? ?: createNewBitmapModelFragment()
    }

    private fun connectBitmapModelFragment() {
        bitmapModel = bitmapModelFragment.bitmapModel
        bitmapModel.listener = this
        mainImageView.scalableBitmapModel = bitmapModel
        parameterAdapter.updateFrom(bitmapModel)
        
        mainImageView.visibility = View.VISIBLE
        mainImageView.invalidate()

        mainImageView.addPlugin(DrawBitmapBoundsPlugin())

        taskProgressBar.apply {
            min = -progressBarZero
            max = progessBarFactor
            isIndeterminate = false
        }
    }

    override fun started() {
        taskProgressBar.apply {
            visibility = View.VISIBLE
            progress = 0
        }
    }

    override fun bitmapUpdated() {
        mainImageView.invalidate()
    }

    override fun finished() {
        taskProgressBar.visibility = View.INVISIBLE
    }

    override fun propertiesChanged(src: FractBitmapModel) {
        parameterAdapter.updateFrom(src)
    }

    override fun setProgress(progress: Float) {
        taskProgressBar.visibility = View.VISIBLE
        taskProgressBar.progress = (progessBarFactor * progress).toInt()
    }

    // Callbacks from Fragments

    fun setImageSize(width: Int, height: Int) {
        bitmapModelFragment.addImageSizeChange(width, height)
    }

    fun setScale(scale: Scale) {
        bitmapModel.scheduleCalcPropertiesChange(ScaleChange(scale))
    }

    fun setParameter(key: String, value: String) {
        try {
            bitmapModel.scheduleCalcPropertiesChange(ParameterChange(key, value))
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }

    fun openParameterEditor(name: String) {
        ParameterEditDialogFragment.newInstance(name, bitmapModel.properties.getParameter(name)).
            show(supportFragmentManager, "dialog")
    }

    fun openScaleEditor() {
        ScaleDialogFragment.
            newInstance(bitmapModel.properties.scale).
            show(supportFragmentManager, "dialog")
    }

    fun openSourceEditor() {
        val sourceCode = bitmapModel.properties.sourceCode
        val parameters = bitmapModel.properties.customParameters

        Intent(this, SourceEditorActivity::class.java).also {
            it.putExtra(SourceEditorActivity.sourceKey, sourceCode)
            it.putExtra(SourceEditorActivity.parametersKey, SourceEditorActivity.toBundle(parameters))
            startActivityForResult(it, sourceRequestCode)
        }
    }

    fun openPaletteEditor(index: Int) {
        val palette = bitmapModel.properties.getPalette(index)

        Intent(this, PaletteEditorActivity::class.java).also {
            it.putExtra(PaletteEditorActivity.paletteKey, PaletteAdapter.toBundle(palette))
            it.putExtra(paletteIndexKey, index)
            startActivityForResult(it, paletteRequestCode)
        }
    }

    fun setSettings(touchEnabled: Boolean, rotationLock: Boolean, confirmZoom: Boolean) {
        mainImageView.hasRotationLock = rotationLock
        mainImageView.isTouchEnabled = touchEnabled
        mainImageView.mustConfirmZoom = confirmZoom
    }

    fun openShaderPropertiesEditor() {
        ShaderPropertiesDialogFragment.newInstance(bitmapModel.properties.shaderProperties).
            show(supportFragmentManager, "dialog")
    }

    fun setShaderProperties(shaderProperties: ShaderProperties) {
        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                return properties.createWithNewBitmapProperties(null, shaderProperties)
            }
        }

        bitmapModel.applyBitmapPropertiesChange(change)
    }

    companion object {
        private const val progessBarFactor = 900
        private const val progressBarZero = 100
        private const val demoSelectorRequestCode = 152
        private const val fractBitmapModelFragmentTag = "bitmapModel"
        private const val saveImageCode = 342
        private const val pngMimeType = "image/png"
        private const val sourceRequestCode = 124
        private const val paletteRequestCode = 571
        private const val paletteIndexKey = "paletteIndex"
        private const val favoritesRequestCode = 412

        const val FILE_PROVIDER = "at.searles.fract.fileprovider"
    }

}