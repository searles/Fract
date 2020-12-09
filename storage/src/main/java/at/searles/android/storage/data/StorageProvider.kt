package at.searles.android.storage.data

import android.content.Context
import android.util.Log
import at.searles.commons.strings.NaturalComparator
import java.io.*
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class StorageProvider(val pathName: String, context: Context) {

    val path: File = context.getDir(pathName, 0)
    val size: Int
        get() = path.listFiles()!!.size

    fun encode(name: String): String {
        return URLEncoder.encode(name, StandardCharsets.UTF_8.toString())
    }

    fun decode(filename: String): String {
        return URLDecoder.decode(filename, StandardCharsets.UTF_8.toString())
    }

    val names
        get() = path.listFiles()!!.map { decode(it.name) }.sortedWith(NaturalComparator)

    fun findPathEntry(name: String): File? {
        return path.listFiles()!!.find { decode(it.name) == name }
    }

    private fun getExistingFileForName(name: String): File {
        return File(path, encode(name)).let {
            if(it.exists()) {
                it
            } else {
                findPathEntry(name) ?: throw FileNotFoundException(it.path)
            }
        }
    }

    /**
     * Throws an exception if there is no entry with 'name'.
     */
    fun load(name: String): String {
        val inFile = getExistingFileForName(name)
        return load(FileInputStream(inFile))
    }

    /**
     * Throws an exception if 'name' is invalid. Existing entries will
     * be overwritten.
     */
    fun save(name: String, value: String) {
        if(name.isEmpty() || name == "." || name == "..") {
            throw InvalidNameException("Bad filename: '$name'", null)
        }

        val outFile = File(path, encode(name))
        save(FileOutputStream(outFile), value)
    }

    fun exists(name: String): Boolean {
        return findPathEntry(name) != null
    }

    fun deleteAll(names: List<String>): List<String> {
        val notDeleted = ArrayList<String>()

        names.forEach {
            if(!getExistingFileForName(it).delete()) {
                Log.i(javaClass.simpleName, "Could not delete $it")
                notDeleted.add(it)
            }
        }

        return notDeleted
    }

    fun rename(oldName: String, newName: String): Boolean {
        val oldFile = getExistingFileForName(oldName)
        val newFile = File(path, encode(newName))

        if(!oldFile.renameTo(newFile)) {
            Log.i(javaClass.simpleName, "Could not rename $oldName to $newName")
            return false
        }

        return true
    }

    fun load(inputStream: InputStream): String {
        inputStream.reader().use {
            return it.readText()
        }
    }

    fun save(outputStream: OutputStream, value: String) {
        outputStream.writer().use {
            it.write(value)
        }
    }


    fun findNextAvailableName(name: String): String {
        var index = 1

        while(true) {
            val newName = "$name ($index)"

            if(!exists(newName)) {
                return newName
            }

            index++
        }
    }

    companion object {
        const val FILE_PROVIDER = "at.searles.storage.fileprovider"
        const val mimeType = "application/zip"
    }
}