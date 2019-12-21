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
import at.searles.android.storage.data.PathContentProvider
import at.searles.commons.math.Scale
import at.searles.fract.demos.AssetsUtils
import at.searles.fract.demos.DemosFolderHolder
import at.searles.fract.editors.*
import at.searles.fract.favorites.AddToFavoritesDialogFragment
import at.searles.fract.favorites.FavoritesProvider
import at.searles.fractbitmapmodel.*
import at.searles.fractbitmapmodel.changes.*
import at.searles.fractimageview.ScalableImageView
import at.searles.fractlang.FractlangProgram
import at.searles.fractlang.semanticanalysis.SemanticAnalysisException
import at.searles.itemselector.ItemSelectorActivity
import at.searles.paletteeditor.Palette
import at.searles.paletteeditor.PaletteEditorActivity
import at.searles.sourceeditor.SourceEditorActivity
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class FractMainActivity : AppCompatActivity(), FractBitmapModelFragment.Listener {

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

    private val mainImageView: ScalableImageView by lazy {
        findViewById<ScalableImageView>(R.id.mainImageView)
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

        with(parameterRecyclerView) {
            adapter = ParameterAdapter(this@FractMainActivity)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    override fun onResume() {
        super.onResume()

        if(!bitmapModelFragment.isInitializing) {
            connectBitmapModelFragment()
            taskProgressBar.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        if(!bitmapModelFragment.isInitializing) {
            bitmapModelFragment.listener = null
        }

        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            demoSelectorRequestCode -> {
                require(data != null)
                val sourceId = data.getStringExtra(ItemSelectorActivity.folderKey)!!
                val parameterId = data.getStringExtra(ItemSelectorActivity.itemKey)!!

                loadDemo(sourceId, parameterId)
            }
            saveImageCode -> {
                require(data != null)
                saveImage(contentResolver.openOutputStream(intent.data!!)!!)
            }
            sourceRequestCode -> {
                val sourceCode = data!!.getStringExtra(SourceEditorActivity.sourceKey)!!
                val parameters = SourceEditorActivity.toStringMap(data.getBundleExtra(SourceEditorActivity.parametersKey)!!)

                setSourceCode(sourceCode, parameters)
                return
            }
            paletteRequestCode -> {
                val palette = data!!.getParcelableExtra(PaletteEditorActivity.paletteKey) as Palette
                val index = data.getIntExtra(paletteIndexKey, -1)

                setPalette(index, palette)
                return
            }
            favoritesRequestCode -> {
                val favoriteKey = data!!.getStringExtra(StorageActivity.nameKey)!!
                loadFavorite(favoriteKey)
                return
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun loadFavorite(favoriteKey: String) {
        FavoritesProvider(this).load(favoriteKey) {
            loadFromJsonString(it)
        }
    }

    private fun loadFromJsonString(jsonString: String) {
        val obj = JSONObject(jsonString)

        val bitmapProperties = BitmapProperties.fromJson(obj)
        val calcProperties = CalcProperties.fromJson(obj)

        bitmapModelFragment.addChange(PropertiesChange(calcProperties, bitmapProperties))
    }

    private fun setPalette(index: Int, palette: Palette) {
        val newPalettes = ArrayList(bitmapModelFragment.palettes).apply {
            this[index] = palette
        }

        bitmapModelFragment.palettes = newPalettes
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
                openAddToFavorites()
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
        )
    }

    private fun openImageSize() {
        ImageSizeDialogFragment.newInstance(
            bitmapModelFragment.bitmap!!.width,
            bitmapModelFragment.bitmap!!.height).show(supportFragmentManager, "dialog")
    }

    private fun saveImage(os: OutputStream) {
        os.use {
            if (!bitmapModelFragment.bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it)) {
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


    private fun openAddToFavorites() {
        AddToFavoritesDialogFragment.newInstance().show(supportFragmentManager, "dialog")
    }

    private fun openFavorites() {
        Intent(this, StorageActivity::class.java).also {
            it.putExtra(StorageActivity.titleKey, resources.getString(R.string.openFavorites))
            it.putExtra(StorageActivity.providerClassNameKey, FavoritesProvider::class.java.canonicalName)
            startActivityForResult(it, favoritesRequestCode)
        }
    }

    private fun loadDemo(sourceKey: String, parametersKey: String) {
        val sourceCode = AssetsUtils.readAssetSource(this, sourceKey)
        val parameters = AssetsUtils.readAssetParameters(this, parametersKey)

        // TODO: It is possible to merge parameters here.

        try {
            val fractlangProgram = FractlangProgram(sourceCode, parameters)
            val change = FractlangWithDefaultsChange(fractlangProgram)
            bitmapModelFragment.addChange(change)
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }

    private fun setSourceCode(sourceCode: String, parameters: Map<String, String>) {
        try {
            val fractlangProgram = FractlangProgram(sourceCode, parameters)
            val change = FractlangChange(fractlangProgram)
            bitmapModelFragment.addChange(change)
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
        val jsonProperties = bitmapModelFragment.createJson()
        val jsonString = jsonProperties.toString(4)

        FavoritesProvider(this).save(name, { jsonString }, false)
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
        bitmapModelFragment.listener = this
    }

    override fun initializationFinished() {
        connectBitmapModelFragment()
    }

    private fun connectBitmapModelFragment() {
        bitmapModel = bitmapModelFragment.bitmapModel
        mainImageView.scalableBitmapModel = bitmapModel

        mainImageView.visibility = View.VISIBLE
        mainImageView.invalidate()

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

    override fun setProgress(progress: Float) {
        taskProgressBar.visibility = View.VISIBLE
        taskProgressBar.progress = (progessBarFactor * progress).toInt()
    }

    // Callbacks from Fragments

    fun setImageSize(width: Int, height: Int) {
        bitmapModelFragment.addImageSizeChange(width, height)
    }

    fun setScale(scale: Scale) {
        bitmapModelFragment.addChange(ScaleChange(scale))
    }

    fun setParameter(key: String, value: String) {
        try {
            bitmapModelFragment.addChange(ParameterChange(key, value))
        } catch(e: SemanticAnalysisException) {
            Toast.makeText(this, getString(R.string.compileError, e.message), Toast.LENGTH_LONG).show()
        }
    }


    fun openParameterEditor(name: String) {
        ParameterEditDialogFragment.newInstance(name, bitmapModel.parameters.getValue(name)).
            show(supportFragmentManager, "dialog")
    }

    fun openScaleEditor() {
        ScaleDialogFragment.
            newInstance(bitmapModelFragment.scale).
            show(supportFragmentManager, "dialog")
    }

    fun openSourceEditor() {
        val sourceCode = bitmapModelFragment.sourceCode
        val parameters = bitmapModelFragment.parameters

        Intent(this, SourceEditorActivity::class.java).also {
            it.putExtra(SourceEditorActivity.sourceKey, sourceCode)
            it.putExtra(SourceEditorActivity.parametersKey, SourceEditorActivity.toBundle(parameters))
            startActivityForResult(it, sourceRequestCode)
        }
    }

    fun openPaletteEditor(index: Int) {
        val palette = bitmapModelFragment.palettes[index]

        Intent(this, PaletteEditorActivity::class.java).also {
            it.putExtra(PaletteEditorActivity.paletteKey, palette)
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
        ShaderPropertiesDialogFragment.newInstance(bitmapModel.shaderProperties).
            show(supportFragmentManager, "dialog")
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