package at.searles.fract

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import at.searles.android.storage.StorageEditor
import at.searles.android.storage.StorageEditorCallback
import at.searles.android.storage.StorageManagerActivity
import at.searles.android.storage.data.StorageProvider
import at.searles.commons.color.Palette
import at.searles.commons.math.Scale
import at.searles.fract.demos.AssetBulkIconGenerator
import at.searles.fract.demos.AssetsUtils
import at.searles.fract.demos.DemosFolderHolder
import at.searles.fract.editors.*
import at.searles.fract.experimental.BulkCalculator
import at.searles.fract.plugins.MoveLightPlugin
import at.searles.fract.plugins.MovePaletteOffsetPlugin
import at.searles.fract.favorites.*
import at.searles.fract.plugins.OrbitPlugin
import at.searles.fractbitmapmodel.*
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.PluginScalableImageView
import at.searles.fractimageview.plugins.DrawBitmapBoundsPlugin
import at.searles.fractimageview.plugins.GestureBlockPlugin
import at.searles.fractimageview.plugins.GridPlugin
import at.searles.fractimageview.plugins.IconIfFlippedPlugin
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.itemselector.ItemSelectorActivity
import at.searles.paletteeditor.PaletteAdapter
import at.searles.paletteeditor.PaletteEditorActivity
import at.searles.sourceeditor.SourceEditorActivity
import com.google.android.material.navigation.NavigationView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.random.Random

// TODO
// * Crash if palette mode is used right after rotation
// * Plugin Coordinates
//     + Calculate 3 corners -1:-1 x 1:1
// * Plugin Move parameters
//     + Show color of dot in parameter view
// * New functions 'ray' and 'straight'[?] and 'plot'
// * Center Lock + Set Center to this parameter if it is a complex value.
// * bitmapUpdate into background thread.
class FractMainActivity : AppCompatActivity(), StorageEditorCallback<FavoriteEntry>, FractBitmapModel.Listener {

    private lateinit var parameterAdapter: ParameterAdapter
    private lateinit var bitmapModelFragment: FractBitmapModelFragment

    lateinit var bitmapModel: FractBitmapModel
        private set

    private lateinit var settings: FractSettings

    // plugins that are controlled via settings
    private lateinit var touchBlockPlugin: GestureBlockPlugin
    private lateinit var lightPlugin: MoveLightPlugin
    private lateinit var palettePlugin: MovePaletteOffsetPlugin
    private lateinit var orbitPlugin: OrbitPlugin
    private lateinit var gridPlugin: GridPlugin

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

    override lateinit var storageProvider: StorageProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        title = ""
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            openNavigationDrawer()
        }

        menuNavigationView.setNavigationItemSelectedListener {
            openMainMenuItem(it)
            true
        }

        initSettings(savedInstanceState)

        try {
            // TODO work around around crash
            initBitmapModelFragment(savedInstanceState)
        } catch(e: IllegalArgumentException) {
            if(e.cause is NoSuchMethodException) {
                val nsme = e.cause as NoSuchMethodException
                Log.e("ERROR", "Reflection not working?", nsme)
                Toast.makeText(this, "Please report to the developer with this message: ${nsme.message}. Thank you.", Toast.LENGTH_LONG).show()
            }

            throw e
        }

        mainImageView.visibility = View.INVISIBLE

        parameterAdapter = ParameterAdapter(this)

        with(parameterRecyclerView) {
            adapter = parameterAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }

        // setting up plugins must happen here because of the life cycle.
        setUpMainImageView()

        storageProvider = StorageProvider(FavoritesStorageManagerActivity.pathName, this)
    }

    private fun openNavigationDrawer() {
        val eyeView = menuNavigationView.findViewById<ImageView>(R.id.eyeView)
        val eyeIds = intArrayOf(R.drawable.eye1, R.drawable.eye2, R.drawable.eye3, R.drawable.eye4, R.drawable.eye5, R.drawable.eye6)
        eyeView.setImageBitmap(BitmapFactory.decodeResource(resources, eyeIds[Random.nextInt(eyeIds.size)]))
        drawerLayout.openDrawer(menuNavigationView)

    }

    override fun onStart() {
        super.onStart()
        attachBitmapModelFragment() // create bitmapModel
        updateSettings()
    }

    private fun initSettings(savedInstanceState: Bundle?) {
        if(savedInstanceState != null) {
            settings = savedInstanceState.getParcelable(settingsKey)!!
            return
        }

        settings = FractSettings()

        // TODO: Permanently save settings

        // Some settings require the bitmap model that is only available after calling
        // onStart, therefore no call to updateSettings here.
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        updateModeMenuIcon()

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.forward -> if(bitmapModel.hasForwardHistory()) {
                bitmapModel.historyForward()
                true
            } else {
                Toast.makeText(this, "No further elements in the history", Toast.LENGTH_LONG).show()
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
            R.id.none -> {
                settings = settings.withMode(FractSettings.Mode.None)
                updateSettings()
                true
            }
            R.id.scale -> {
                settings = settings.withMode(FractSettings.Mode.Scale)
                updateSettings()
                true
            }
            R.id.light -> {
                settings = settings.withMode(FractSettings.Mode.Light)
                updateSettings()
                true
            }
            R.id.palette -> {
                settings = settings.withMode(FractSettings.Mode.Palette)
                updateSettings()
                true
            }
            R.id.orbit -> {
                settings = settings.withMode(FractSettings.Mode.Orbit)
                updateSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
            sourceRequestCode -> {
                val sourceCode = intent!!.getStringExtra(SourceEditorActivity.sourceKey)!!
                val parameters = SourceEditorActivity.toStringMap(intent.getBundleExtra(SourceEditorActivity.parametersKey)!!)

                setSourceCode(sourceCode, parameters)
                return
            }
            paletteRequestCode -> {
                val palette = PaletteAdapter.toPalette(intent!!.getBundleExtra(PaletteEditorActivity.paletteKey)!!)
                val label = intent.getStringExtra(paletteLabelKey)!!

                setPalette(label, palette)
                return
            }
            favoritesRequestCode -> {
                val favoriteKey = intent!!.getStringExtra(StorageManagerActivity.nameKey)!!

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

        if(mainImageView.cancelMultitouchGesture()) {
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
            storageEditor.forceOpen(favoriteKey)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, resources.getText(R.string.errorWithMsg, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun setPalette(label: String, palette: Palette) {
        val change = object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val palettes = properties.customPalettes.toMutableMap()

                palettes[label] = palette

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
            type = pngMimeType
        }

        startActivity(Intent.createChooser(shareIntent, resources.getString(R.string.shareImage)))
    }

    private fun openSaveImageToGallery() {
        // Check permissions
        if (android.os.Build.VERSION.SDK_INT < 29) {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )

                return
            }
        }

        SaveImageDialogFragment.newInstance().show(supportFragmentManager, "dialog")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO If permission is needed for anything else
                    openSaveImageToGallery()
                } else {
                    Toast.makeText(this, getString(R.string.grantPermissionExplanation), Toast.LENGTH_LONG).show()
                }

                return
            }
        }
    }

    private fun openAddToFavoritesDialog() {
        storageEditor.onSaveAs()
    }

    private fun openFavorites() {
        storageEditor.onOpen(favoritesRequestCode)
    }

    private fun loadDemo(sourceKey: String, parametersKey: String, mergeParameters: Boolean) {
        val sourceCode = AssetsUtils.readAssetSource(this, sourceKey)
        val parameterSet = AssetsUtils.readAssetParameters(this, parametersKey)
        val currentProperties = bitmapModel.properties

        val parameters = HashMap<String, String>()

        val scale: Scale?
        val palettes: Map<String, Palette>

        if(mergeParameters) {
            Toast.makeText(this, "Merging sample with current parameters", Toast.LENGTH_SHORT).show()
            parameters.putAll(currentProperties.customParameters)

            scale = if(!currentProperties.isDefaultScale) currentProperties.scale else parameterSet.scale
            palettes = currentProperties.customPalettes
        } else {
            scale = parameterSet.scale
            palettes = emptyMap()
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
        return FractBitmapModelFragment.createInstance(properties, settings.width, settings.height).apply {
            supportFragmentManager.beginTransaction().add(this, fractBitmapModelFragmentTag).commit()
        }
    }

    private fun initBitmapModelFragment(savedInstanceState: Bundle?) {
        // Settings must be initialized
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

    private fun attachBitmapModelFragment() {
        bitmapModel = bitmapModelFragment.bitmapModel
        bitmapModel.listener = this

        taskProgressBar.apply {
            min = -progressBarZero
            max = progressBarFactor
        }

        if(!bitmapModel.isTaskRunning) {
            taskProgressBar.visibility = View.INVISIBLE
        }

        parameterAdapter.updateFrom(bitmapModel)
        mainImageView.bitmapModel = bitmapModel
    }

    private fun setUpMainImageView() {
        mainImageView.visibility = View.VISIBLE

        mainImageView.addPlugin(IconIfFlippedPlugin(this))
        mainImageView.addPlugin(DrawBitmapBoundsPlugin())

        touchBlockPlugin = GestureBlockPlugin()

        lightPlugin = MoveLightPlugin { bitmapModel }
        palettePlugin = MovePaletteOffsetPlugin({ settings }, { bitmapModel })
        orbitPlugin = OrbitPlugin(this, mainImageView)
        gridPlugin = GridPlugin(this)

        mainImageView.addPlugin(touchBlockPlugin)
        mainImageView.addPlugin(lightPlugin)
        mainImageView.addPlugin(palettePlugin)
        mainImageView.addPlugin(orbitPlugin)

        mainImageView.addPlugin(gridPlugin)
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
        // TODO Mediator
        parameterAdapter.updateFrom(src)
    }

    override fun setProgress(progress: Float) {
        with(taskProgressBar) {
            visibility = View.VISIBLE
            this.progress = (progressBarFactor * progress).toInt()
            isIndeterminate = false
        }
    }

    // Callbacks from Fragments

    fun setImageSize(width: Int, height: Int) {
        if(bitmapModelFragment.addImageSizeChange(width, height)) {
            // no need to call updateSettings
            settings = settings.withSize(width, height)
        }
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

    fun openPaletteEditor(label: String) {
        val palette = bitmapModel.properties.getPalette(label)

        Intent(this, PaletteEditorActivity::class.java).also {
            it.putExtra(PaletteEditorActivity.paletteKey, PaletteAdapter.toBundle(palette))
            it.putExtra(paletteLabelKey, label)
            startActivityForResult(it, paletteRequestCode)
        }
    }

    fun getSettings(): FractSettings {
        return settings
    }

    fun getBitmap(): Bitmap {
        return bitmapModel.bitmap
    }

    fun setSettings(settings: FractSettings) {
        this.settings = settings
        updateSettings()
    }

    private fun updateModeMenuIcon() {
        val menu = toolbar.menu.findItem(R.id.modeMenu) ?: return

        menu.setIcon(
            when(settings.mode) {
                FractSettings.Mode.None -> R.drawable.ic_none
                FractSettings.Mode.Scale -> R.drawable.ic_zoom
                FractSettings.Mode.Light -> R.drawable.ic_light
                FractSettings.Mode.Orbit -> R.drawable.ic_orbit
                else -> R.drawable.ic_color
            }
        )
    }

    private fun updateSettings() {
        mainImageView.hasRotationLock = settings.isRotationLock
        mainImageView.mustConfirmZoom = settings.isConfirmZoom

        touchBlockPlugin.isEnabled = settings.mode == FractSettings.Mode.None
        lightPlugin.isEnabled = settings.mode == FractSettings.Mode.Light
        palettePlugin.isEnabled = settings.mode == FractSettings.Mode.Palette
        orbitPlugin.isEnabled = settings.mode == FractSettings.Mode.Orbit

        updateModeMenuIcon()

        if(settings.mode == FractSettings.Mode.Light) {
            // TODO move this
            val oldShaderProperties = bitmapModel.properties.shaderProperties
            if(!oldShaderProperties.useLightEffect) {
                val newShaderProperties =
                    ShaderProperties(true, oldShaderProperties.polarAngle,
                        oldShaderProperties.azimuthAngle, oldShaderProperties.ambientReflection,
                        oldShaderProperties.diffuseReflection, oldShaderProperties.specularReflection,
                        oldShaderProperties.shininess)

                val change = object : BitmapPropertiesChange {
                    override fun accept(properties: FractProperties): FractProperties {
                        return properties.createWithNewBitmapProperties(null, newShaderProperties)
                    }
                }

                bitmapModel.applyBitmapPropertiesChange(change)
            }
        }

        gridPlugin.isEnabled = settings.isGridEnabled

        mainImageView.invalidate()
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
        // this is called from save image-dialog
        storageEditor.saveAs(name)
    }

    fun openParameterContext(name: String) {
        ParameterContextDialogFragment.newInstance(name, bitmapModel.properties.getParameter(name)).
            show(supportFragmentManager, "dialog")
    }

    fun openParameterBoolContext(name: String) {
        ParameterContextDialogFragment.newInstance(name, bitmapModel.properties.getParameter(name)).
            show(supportFragmentManager, "dialog")
    }

    fun openPaletteContext(label: String) {
        PaletteContextDialogFragment.newInstance(label, bitmapModel.properties.getPalette(label)).
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

    fun resetPalette(label: String) {
        bitmapModel.applyBitmapPropertiesChange(object: BitmapPropertiesChange {
            override fun accept(properties: FractProperties): FractProperties {
                val customPalettes = properties.customPalettes.toMutableMap()
                customPalettes.remove(label)
                return FractProperties(properties.program, properties.customScale, properties.customShaderProperties, customPalettes)
            }
        })

        parameterAdapter.updateFrom(bitmapModel)
    }

    fun updateParameterAdapter() {
        parameterAdapter.updateFrom(bitmapModel)
    }

    override val storageEditor: StorageEditor<FavoriteEntry>
        get() = FavoritesStorageEditor(this, this, storageProvider)

    override var value: FavoriteEntry
        get() = FavoriteEntry.create(bitmapModel)
        set(value) {
            bitmapModel.scheduleCalcPropertiesChange(NewFractPropertiesChange(value.properties))
        }

    override fun onStorageItemChanged(name: String?, isModified: Boolean) {
        // ignore.
        // TODO Maybe keep name?
    }

    companion object {
        private const val progressBarFactor = 900
        private const val progressBarZero = 100
        private const val demoSelectorRequestCode = 152
        private const val fractBitmapModelFragmentTag = "bitmapModel"
        private const val sourceRequestCode = 124
        private const val paletteRequestCode = 571
        private const val paletteLabelKey = "paletteIndex"
        private const val favoritesRequestCode = 412

        private const val settingsKey = "settings"
        private const val propertiesKey = "properties"

        const val FILE_PROVIDER = "at.searles.storage.fileprovider"

        const val WEB_PAGE_URI = "http://fractapp.wordpress.com/"

        private const val pngMimeType = "image/png"
    }
}