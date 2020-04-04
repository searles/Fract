package at.searles.fract

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.searles.android.storage.StorageActivity
import at.searles.android.storage.data.PathContentProvider
import at.searles.android.storage.dialog.ReplaceExistingDialogFragment
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fract.demos.AssetBulkIconGenerator
import at.searles.fract.demos.AssetsUtils
import at.searles.fract.demos.DemosFolderHolder
import at.searles.fract.editors.*
import at.searles.fract.experimental.BulkCalculator
import at.searles.fract.favorites.AddToFavoritesDialogFragment
import at.searles.fract.favorites.FavoritesProvider
import at.searles.fractbitmapmodel.*
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.DrawBitmapBoundsPlugin
import at.searles.fractimageview.GridPlugin
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class FractMainActivity : AppCompatActivity(), FractBitmapModel.Listener, ReplaceExistingDialogFragment.Callback {

    private lateinit var parameterAdapter: ParameterAdapter
    private lateinit var bitmapModelFragment: FractBitmapModelFragment
    private lateinit var bitmapModel: FractBitmapModel

    private lateinit var settings: FractSettings

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

    private val toolbar: Toolbar by lazy {
        findViewById<Toolbar>(R.id.toolbar)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        title = ""
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(menuNavigationView)
        }

        menuNavigationView.setNavigationItemSelectedListener {
            openMainMenuItem(it)
            true
        }

        initSettings(savedInstanceState)
        initBitmapModelFragment(savedInstanceState)

        mainImageView.visibility = View.INVISIBLE

        parameterAdapter = ParameterAdapter(this)

        with(parameterRecyclerView) {
            adapter = parameterAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onStart() {
        super.onStart()
        connectBitmapModelFragment()
    }

    private fun initSettings(savedInstanceState: Bundle?) {
        if(savedInstanceState != null) {
            settings = savedInstanceState.getParcelable(settingsKey)!!
            updateSettings()
            return
        }

        settings = FractSettings()
        updateSettings()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.forward -> if(bitmapModel.hasForwardHistory()) {
                bitmapModel.historyForward()
                true
            } else {
                Toast.makeText(this, "No further elements in the history", Toast.LENGTH_LONG).show()
                // TODO Disable menu
                false
            }
            R.id.openPropertiesDrawer -> {
                drawerLayout.openDrawer(parameterRecyclerView)
                true
            }
            R.id.bulkCreateIcons -> {
                BulkCalculator(this, AssetBulkIconGenerator(this)).start()
                true
            }
            else -> error("bad item: $item")
        }
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

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(menuNavigationView)) {
            drawerLayout.closeDrawer(menuNavigationView)
            return
        }

        if(drawerLayout.isDrawerOpen(parameterRecyclerView)) {
            drawerLayout.closeDrawer(parameterRecyclerView)
            return
        }

        if(!bitmapModelFragment.bitmapModel.hasBackHistory()) {
            Toast.makeText(this, "History is empty.", Toast.LENGTH_LONG).show()
            return
        }

        bitmapModelFragment.bitmapModel.historyBack()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(settingsKey, settings)
        outState.putBundle(propertiesKey, FractPropertiesAdapter.toBundle(bitmapModel.properties))
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
        parameterAdapter.updateFrom(bitmapModel)
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
            R.id.saveToGalleryAction -> {
                openSaveImageToGallery()
            }
            R.id.imageSize -> {
                openImageSize()
            }
            R.id.openSettingsAction -> {
                openSettings()
            }
            R.id.openBlog -> {
                openBlog()
            }
        }

        drawerLayout.closeDrawers()
    }

    private fun openBlog() {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(WEB_PAGE_URI)
            )
        )
    }

    private fun openSettings() {
        SettingsDialogFragment.newInstance(
            settings
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

    private fun openSaveImageToGallery() {
        // TODO Create Fract folder?
        // TODO https://stackoverflow.com/questions/60798804/store-image-via-android-media-store-in-new-folder
        // TODO https://stackoverflow.com/questions/57726896/mediastore-images-media-insertimage-deprecated
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val timestamp = format.format(System.currentTimeMillis())

        MediaStore.Images.Media.insertImage(contentResolver, bitmapModel.bitmap, "Fract-$timestamp", "Image created in Fract on $timestamp");
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
        val parameterSet = AssetsUtils.readAssetParameters(this, parametersKey)
        val currentProperties = bitmapModel.properties

        val parameters = HashMap<String, String>()

        val scale: Scale?
        val palettes: List<Palette?>

        if(mergeParameters) {
            Toast.makeText(this, "Merging sample with current parameters", Toast.LENGTH_SHORT).show()
            parameters.putAll(currentProperties.customParameters)

            scale = if(!currentProperties.isDefaultScale) currentProperties.scale else parameterSet.scale
            palettes = (0 until currentProperties.paletteCount).map {
                if(currentProperties.isDefaultPalette(it)) null else currentProperties.getPalette(it)
            }
        } else {
            scale = parameterSet.scale
            palettes = emptyList()
        }

        parameters.putAll(parameterSet.parameters)

        try {
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

    private fun createNewBitmapModelFragment(properties: FractProperties): FractBitmapModelFragment {
        // use default source
        // TODO manage dimensions via settings
        return FractBitmapModelFragment.createInstance(properties, FactorySettings.factoryWidth, FactorySettings.factoryHeight).apply {
            supportFragmentManager.beginTransaction().add(this, fractBitmapModelFragmentTag).commit()
        }
    }

    private fun initBitmapModelFragment(savedInstanceState: Bundle?) {
        val fragment = supportFragmentManager.findFragmentByTag(fractBitmapModelFragmentTag)

        if(fragment != null) {
            bitmapModelFragment = fragment as FractBitmapModelFragment
            return
        }

        if(savedInstanceState != null) {
            val properties = FractPropertiesAdapter.fromBundle(savedInstanceState.getBundle(propertiesKey)!!)
            bitmapModelFragment = createNewBitmapModelFragment(properties)
            return
        }

        bitmapModelFragment = createNewBitmapModelFragment(FactorySettings.getStartupFractal(this))
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
        }

        if(!bitmapModel.isTaskRunning) {
            taskProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun started() {
        taskProgressBar.apply {
            visibility = View.VISIBLE
            isIndeterminate = true
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
        with(taskProgressBar) {
            visibility = View.VISIBLE
            this.progress = (progessBarFactor * progress).toInt()
            isIndeterminate = false
        }
    }

    // Callbacks from Fragments

    fun setImageSize(width: Int, height: Int) {
        bitmapModelFragment.addImageSizeChange(width, height)
    }

    fun setScale(scale: Scale) {
        bitmapModel.scheduleCalcPropertiesChange(ScaleChange(scale))
    }

    fun setParameter(key: String, value: String) {
        bitmapModel.scheduleCalcPropertiesChange(ParameterChange(key, value))
    }

    fun openParameterEditor(name: String) {
        ParameterDialogFragment.newInstance(name, bitmapModel.properties.getParameter(name)).
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

    // XXX: If there are more plugins, manage them in a different way.
    var gridPlugin: GridPlugin? = null

    fun setSettings(settings: FractSettings) {
        this.settings = settings
        updateSettings()
    }

    private fun updateSettings() {
        mainImageView.hasRotationLock = settings.isRotationLock
        mainImageView.isTouchEnabled = settings.isTouchEnabled
        mainImageView.mustConfirmZoom = settings.isConfirmZoom

        if(settings.isGridEnabled) {
            if(gridPlugin == null) {
                GridPlugin(this).also {
                    gridPlugin = it
                    mainImageView.addPlugin(it)
                    mainImageView.invalidate()
                }
            }
        } else {
            with(gridPlugin) {
                if(this != null) {
                    mainImageView.removePlugin(this)
                    gridPlugin = null
                    mainImageView.invalidate()
                }
            }
        }
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
        parameterAdapter.updateFrom(bitmapModel)
    }

    fun saveToFavorites(name: String) {
        FavoritesProvider(this).saveToFavorites(name, bitmapModel, false)
    }

    override fun replaceExistingAndSave(name: String) {
        FavoritesProvider(this).saveToFavorites(name, bitmapModel, true)
    }


    fun openParameterContext(name: String) {
        ParameterContextDialogFragment.newInstance(name, bitmapModel.properties.getParameter(name)).
            show(supportFragmentManager, "dialog")
    }

    fun openParameterBoolContext(name: String) {
        ParameterContextDialogFragment.newInstance(name, bitmapModel.properties.getParameter(name)).
            show(supportFragmentManager, "dialog")
    }

    fun openPaletteContext(index: Int) {
        PaletteContextDialogFragment.newInstance(index, bitmapModel.properties.getPalette(index)).
            show(supportFragmentManager, "dialog")
    }

    fun openScaleContext() {
        ScaleContextDialogFragment.newInstance(bitmapModel.properties.scale).
            show(supportFragmentManager, "dialog")
    }

    fun openShaderPropertiesContext() {
        ShaderPropertiesContextDialogFragment.newInstance(bitmapModel.properties.shaderProperties).
            show(supportFragmentManager, "dialog")
    }

    fun resetScale() {
        bitmapModel.scheduleCalcPropertiesChange(object: CalcPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                return FractProperties(properties.program, null, properties.customShaderProperties, properties.customPalettes)
            }
        })
    }

    fun setParameterToCenter(key: String) {
        try {
            val scaleString = "${bitmapModel.properties.scale.cx} : ${bitmapModel.properties.scale.cy}"
            bitmapModel.scheduleCalcPropertiesChange(ParameterChange(key, scaleString))
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }

    fun resetParameter(key: String) {
        try {
            bitmapModel.scheduleCalcPropertiesChange(ParameterResetChange(key))
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }

    fun resetShaderProperties() {
        bitmapModel.applyBitmapPropertiesChange(object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                return FractProperties(properties.program, properties.customScale, null, properties.customPalettes)
            }
        })

        parameterAdapter.updateFrom(bitmapModel)
    }

    fun resetPalette(index: Int) {
        bitmapModel.applyBitmapPropertiesChange(object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val customPalettes = properties.customPalettes.toMutableList()

                if(customPalettes.size >= index) {
                    customPalettes[index] = null
                }

                return FractProperties(properties.program, properties.customScale, properties.customShaderProperties, customPalettes)
            }
        })

        parameterAdapter.updateFrom(bitmapModel)
    }

    fun updateParameterAdapter() {
        parameterAdapter.updateFrom(bitmapModel)
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

        private val settingsKey = "settings"
        private val propertiesKey = "properties"

        const val FILE_PROVIDER = "at.searles.storage.fileprovider"

        const val WEB_PAGE_URI = "http://fractapp.wordpress.com/"
    }
}