package sh.bitsy.app.kutility.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
	private var pendingSaveJob: Job? = null

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
					mapCache = jsonSerializer.decodeFromString<Map<String, String>>(fileContent).toMutableMap()
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
		val currentMapToSave = mapCache?.toMap() ?: return
		fileSaveMutex.withLock {
			try {
				val file = File("$fullPath.tmp")
				file.parentFile?.mkdirs()
				val jsonString = jsonSerializer.encodeToString(currentMapToSave)
				file.writeText(jsonString)
				val originalFile = File(fullPath)
				if (originalFile.exists()) {
					originalFile.delete()
				}
				file.renameTo(originalFile)
			} catch (e: IOException) {
				System.err.println("Error saving settings to $fullPath: ${e.message}")
			} catch (e: SerializationException) {
				System.err.println("Error serializing settings to $fullPath: ${e.message}")
			}
		}
	}

	private suspend inline fun <T> accessMapWrite(
		waitForSave: Boolean,
		isWriteOperation: Boolean = true,
		crossinline block: suspend (settingsMap: MutableMap<String, String>) -> T
	): T = settingsAccessMutex.withLock {
		val map = loadMapFromFile()
		val result = block(map)

		// Save changes if it was a write operation
		if (isWriteOperation) {
			if (waitForSave) {
				pendingSaveJob?.cancel()
				pendingSaveJob = null
				saveMapToFile()
			} else {
				pendingSaveJob?.cancel()
				pendingSaveJob = coroutineScope.launch {
					saveMapToFile()
				}
			}
		}

		return result
	}

	private suspend inline fun <T> accessMapRead(crossinline block: suspend (settingsMap: Map<String, String>) -> T	): T =
		accessMapWrite(isWriteOperation = false, waitForSave = false, block = block)

	// =============================================
	// Get operations
	// =============================================

	suspend fun getStringOrThrow(key: String): String = accessMapRead { map ->
		map[key] ?: throw IllegalArgumentException("Key '$key' not found in local storage")
	}

	suspend inline fun <reified T : @Serializable Any> getOrThrow(key: String): T = getOrThrow(key, typeOf<T>()) as T

	suspend fun <T : @Serializable Any> getOrThrow(key: String, kType: KType): T = accessMapRead { map ->
		val str = map[key] ?: throw IllegalArgumentException("Key '$key' not found in local storage")
		try {
			jsonSerializer.decodeFromString(serializer(kType), str) as T
		} catch (e: SerializationException) {
			throw IllegalArgumentException("Serialization error for key '$key' and saved json '$str' : ${e.message}")
		} catch (e: IllegalArgumentException) {
			throw IllegalArgumentException("Key '$key' cannot be represented with type '$kType' : ${e.message}")
		}
	}

	suspend fun getStringOrPut(key: String, value: String, waitForSave: Boolean = false): String = accessMapWrite(waitForSave) { map ->
		map.getOrPut(key) { value }
	}

	suspend inline fun <reified T : @Serializable Any> getOrPut(key: String, value: T, waitForSave: Boolean = false): T = getOrPut(key, value, waitForSave, typeOf<T>()) as T

	suspend fun <T : @Serializable Any> getOrPut(key: String, value: T, waitForSave: Boolean = false, kType: KType): T = accessMapWrite(waitForSave) { map ->
		val serializer = serializer(kType)
		try {
			val str = map.getOrPut(key) { jsonSerializer.encodeToString(serializer, value) }
			jsonSerializer.decodeFromString(serializer, str) as T
		} catch (_: Exception) {
			map.put(key, jsonSerializer.encodeToString(serializer, value))
			value
		}
	}

	suspend fun getStringOrNull(key: String): String? = accessMapRead { map ->
		map[key]
	}

	suspend inline fun <reified T : @Serializable Any> getOrNull(key: String): T? = getOrNull(key, typeOf<T>()) as T?

	suspend fun <T : @Serializable Any> getOrNull(key: String, kType: KType): T? = accessMapRead { map ->
		try {
			val str = map[key] ?: return@accessMapRead null
			jsonSerializer.decodeFromString(serializer(kType), str) as T
		} catch (_: Exception) {
			null
		}
	}

	suspend fun getAll(): Map<String, String> = accessMapRead { map ->
		map.toMap()
	}

	// =============================================
	// Set operations
	// =============================================

	suspend fun putString(key: String, value: String, waitForSave: Boolean = false): String? = accessMapWrite(waitForSave) { map ->
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
	): String? = accessMapWrite(waitForSave) { map ->
		map.put(key, jsonSerializer.encodeToString(serializer(kType), value))
	}

	// ===============================================
	// Remove operations
	// ===============================================

	suspend fun remove(key: String, waitForSave: Boolean = false): String? = accessMapWrite(waitForSave) { map ->
		map.remove(key)
	}

	suspend fun clear(waitForSave: Boolean = false) = accessMapWrite(waitForSave) { map ->
		map.clear()
	}

	// ===============================================
	// Other operations
	// ===============================================

	suspend fun contains(key: String): Boolean = accessMapRead { map ->
		map.containsKey(key)
	}

	suspend fun flush() {
		settingsAccessMutex.withLock {
			pendingSaveJob?.join()
			pendingSaveJob = null
		}
	}
}