package sh.bitsy.app.kutility.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class LocalStorage {
    abstract val companyName: String
    abstract val appName: String
    abstract val fileName: String
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val settingsAccessMutex = Mutex()
    private val fileSaveMutex = Mutex()
    private val jsonSerializer = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private var mapCache: MutableMap<String, String>? = null

    private val fullPath: String by lazy {
        val os = System.getProperty("os.name", "unknown").lowercase()
        val userHome = System.getProperty("user.home", ".")

        // OS-specific paths
        val appDataRootPath = when {
            os.contains("win") -> System.getenv("APPDATA") ?: File(userHome, "AppData/Roaming").path
            os.contains("mac") -> File(userHome, "Library/Application Support").path
            os.contains("nix") || os.contains("nux") -> File(userHome, ".config").path
            else -> File(userHome, ".$companyName.$appName").path // fallback
        }

        val appDataRootDir = File(appDataRootPath)
        val companyDir = File(appDataRootDir, companyName)
        val appDir = File(companyDir, appName)

        // Ensure the directory structure exists
        if (!appDir.exists()) {
            appDir.mkdirs()
        }

        File(appDir, "$fileName.json").absolutePath
    }

    private suspend fun loadMapFromFile(): MutableMap<String, String> {
        if (mapCache != null) {
            return mapCache!!
        } else {
            return withContext(Dispatchers.IO) {
                val file = File(fullPath)
                if (!file.exists() || file.length() == 0L) {
                    mapCache = mutableMapOf()
                    return@withContext mapCache!!
                }
                try {
                    val fileContent = file.readText()
                    if (fileContent.isBlank()) {
                        mapCache = mutableMapOf()
                        return@withContext mapCache!!
                    }
                    mapCache = jsonSerializer.decodeFromString<Map<String, String>>(fileContent)
                        .toMutableMap()
                    mapCache!!
                } catch (e: IOException) {
                    System.err.println("Error loading settings from $fullPath: ${e.message}")
                    mapCache = mutableMapOf()
                    mapCache!!
                } catch (e: SerializationException) {
                    System.err.println("Error deserializing settings from $fullPath (file might be corrupted): ${e.message}")
                    File(fullPath).renameTo(File("$fullPath.corrupted.${System.currentTimeMillis()}"))
                    mapCache = mutableMapOf()
                    mapCache!!
                }
            }
        }
    }

    private suspend fun saveMapToFile() {
        fileSaveMutex.withLock {
            try {
                val file = File(fullPath)
                file.parentFile?.mkdirs()
                val jsonString = jsonSerializer.encodeToString(mapCache!!)
                file.writeText(jsonString)
            } catch (e: IOException) {
                System.err.println("Error saving settings to $fullPath: ${e.message}")
            } catch (e: SerializationException) {
                System.err.println("Error serializing settings to $fullPath: ${e.message}")
            }
        }
    }

    private suspend inline fun <T> accessSettings(
        isWriteOperation: Boolean,
        waitForSave: Boolean = false,
        crossinline block: suspend (settingsMap: MutableMap<String, String>) -> T
    ): T {
        return settingsAccessMutex.withLock {
            val map = loadMapFromFile()
            val result = block(map)

            // Save changes if it was a write operation
            if (isWriteOperation) {
                if (waitForSave) {
                    saveMapToFile()
                } else {
                    coroutineScope.launch {
                        saveMapToFile()
                    }
                }
            }

            result
        }
    }

    // =============================================
    // Get operations
    // =============================================

    suspend fun getOrThrow(key: String): String = accessSettings(isWriteOperation = false) { map ->
        map[key] ?: throw IllegalArgumentException("Key '$key' not found in local storage")
    }

    suspend fun getOrPut(key: String, value: String, waitForSave: Boolean = false): String =
        accessSettings(isWriteOperation = true, waitForSave) { map ->
            map.getOrPut(key) { value }
        }

    suspend fun getOrNull(key: String): String? = accessSettings(isWriteOperation = false) { map ->
        map[key]
    }

    suspend fun getOrDefault(key: String, defaultValue: String): String =
        accessSettings(isWriteOperation = false) { map ->
            map.getOrDefault(key, defaultValue)
        }

    suspend fun getAll(): Map<String, String> = accessSettings(isWriteOperation = false) { map ->
        map.toMap()
    }

    // =============================================
    // Set operations
    // =============================================

    suspend fun put(key: String, value: String, waitForSave: Boolean = false): String? =
        accessSettings(isWriteOperation = true, waitForSave) { map ->
            map.put(key, value)
        }

    suspend inline fun <reified T : @Serializable Any> put(
        key: String,
        value: T,
        waitForSave: Boolean = false
    ): String? = put(key, value, typeOf<T>(), waitForSave)

    suspend fun <T : @Serializable Any> put(
        key: String,
        value: T,
        kType: KType,
        waitForSave: Boolean = false
    ): String? = accessSettings(isWriteOperation = true, waitForSave) { map ->
        map.put(key, jsonSerializer.encodeToString(serializer(kType), value))
    }

    // ===============================================
    // Info operations
    // ===============================================

    suspend fun contains(key: String): Boolean = accessSettings(isWriteOperation = false) { map ->
        map.containsKey(key)
    }

    // ===============================================
    // Remove operations
    // ===============================================

    suspend fun remove(key: String, waitForSave: Boolean = false): String? =
        accessSettings(isWriteOperation = true, waitForSave) { map ->
            map.remove(key)
        }

    suspend fun clear(waitForSave: Boolean = false) =
        accessSettings(isWriteOperation = true, waitForSave) { map ->
            map.clear()
        }
}