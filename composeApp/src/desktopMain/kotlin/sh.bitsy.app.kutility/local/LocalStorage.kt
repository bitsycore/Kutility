@file:Suppress("unused")

package sh.bitsy.app.kutility.local

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class LocalStorage(val filename: String, val path: File) {
	private val coroutineScope = CoroutineScope(Dispatchers.IO)
	private val settingsAccessMutex = Mutex()
	private val fileSaveMutex = Mutex()
	private val jsonSerializer = Json { prettyPrint = true; ignoreUnknownKeys = true }
	private var mapCache: MutableMap<String, JsonElement>? = null
	private var pendingSaveJob: Job? = null

	private val file: File by lazy {
		File(path, "$filename.json")
	}

	private suspend fun loadMapFromFile(): MutableMap<String, JsonElement> {
		if (mapCache != null) {
			return mapCache!!
		} else {
			return withContext(Dispatchers.IO) {
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
					mapCache = jsonSerializer.decodeFromString<Map<String, JsonElement>>(fileContent).toMutableMap()
					mapCache!!
				} catch (e: IOException) {
					System.err.println("Error loading settings from $file: ${e.message}")
					mapCache = mutableMapOf()
					mapCache!!
				} catch (e: SerializationException) {
					System.err.println("Error deserializing settings from $file (file might be corrupted): ${e.message}")
					file.renameTo(File("$file.corrupted.${System.currentTimeMillis()}"))
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
				val tempFile = File("${file.absolutePath}.tmp")
				tempFile.parentFile?.mkdirs()
				val jsonString = jsonSerializer.encodeToString(currentMapToSave)
				tempFile.writeText(jsonString)
				if (file.exists()) {
					file.delete()
				}
				tempFile.renameTo(file)
			} catch (e: IOException) {
				System.err.println("Error saving settings to $file: ${e.message}")
			} catch (e: SerializationException) {
				System.err.println("Error serializing settings to $file: ${e.message}")
			}
		}
	}

	private suspend inline fun <T> accessMapWrite(
		waitForSave: Boolean,
		isWriteOperation: Boolean = true,
		crossinline block: suspend (settingsMap: MutableMap<String, JsonElement>) -> T
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

	private suspend inline fun <T> accessMapRead(crossinline block: suspend (settingsMap: Map<String, JsonElement>) -> T): T =
		accessMapWrite(isWriteOperation = false, waitForSave = false, block = block)

	// =============================================
	// Get operations
	// =============================================

	suspend inline fun <reified T : Any> getOrThrow(key: String): T = getOrThrow(key, typeOf<T>())

	suspend fun <T : Any> getOrThrow(key: String, kType: KType): T = accessMapRead { map ->
		val str = map[key] ?: throw IllegalArgumentException("Key '$key' not found in local storage")
		try {
			@Suppress("UNCHECKED_CAST")
			jsonSerializer.decodeFromJsonElement(serializer(kType), str) as T
		} catch (e: SerializationException) {
			throw IllegalArgumentException("Serialization error for key '$key' and saved json '$str' : ${e.message}")
		} catch (e: IllegalArgumentException) {
			throw IllegalArgumentException("Key '$key' cannot be represented with type '$kType' : ${e.message}")
		}
	}


	suspend inline fun <reified T : Any> getOrPut(key: String, value: T, waitForSave: Boolean = false): T =
		getOrPut(key, value, waitForSave, typeOf<T>())

	suspend fun <T : Any> getOrPut(key: String, value: T, waitForSave: Boolean = false, kType: KType): T = accessMapWrite(waitForSave) { map ->
		val serializer = serializer(kType)
		try {
			val str = map.getOrPut(key) { jsonSerializer.encodeToJsonElement(serializer, value) }
			@Suppress("UNCHECKED_CAST")
			jsonSerializer.decodeFromJsonElement(serializer, str) as T
		} catch (_: Exception) {
			map.put(key, jsonSerializer.encodeToJsonElement(serializer, value))
			value
		}
	}

	suspend inline fun <reified T : Any> getOrNull(key: String): T? = getOrNull(key, typeOf<T>()) as T?

	suspend fun <T : Any> getOrNull(key: String, kType: KType): T? = accessMapRead { map ->
		try {
			val str = map[key] ?: return@accessMapRead null
			@Suppress("UNCHECKED_CAST")
			jsonSerializer.decodeFromJsonElement(serializer(kType), str) as T
		} catch (_: Exception) {
			null
		}
	}

	suspend fun getAll(): Map<String, String> = accessMapRead { map ->
		map.mapValues { it.toString() }
	}

	// =============================================
	// Set operations
	// =============================================

	suspend inline fun <reified T : Any> put(
		key: String,
		value: T,
		waitForSave: Boolean = false
	): T? = put(key, value, typeOf<T>(), waitForSave)

	suspend fun <T : Any> put(
		key: String,
		value: T,
		kType: KType,
		waitForSave: Boolean = false
	): T? = accessMapWrite(waitForSave) { map ->
		val old = map.put(key, jsonSerializer.encodeToJsonElement(serializer(kType), value))
		if (old == null) {
			return@accessMapWrite null
		}
		@Suppress("UNCHECKED_CAST")
		jsonSerializer.decodeFromJsonElement(serializer(kType), old) as T?
	}

	// ===============================================
	// Remove operations
	// ===============================================

	suspend inline fun <reified T : Any> remove(key: String, waitForSave: Boolean = false): T? = remove(key, waitForSave, typeOf<T>())

	suspend fun <T : Any> remove(key: String, waitForSave: Boolean = false, kType: KType): T? = accessMapWrite(waitForSave) { map ->
		val serializer = serializer(kType)
		val old = map.remove(key)
		if (old == null) {
			return@accessMapWrite null
		}
		@Suppress("UNCHECKED_CAST")
		jsonSerializer.decodeFromJsonElement(serializer, old) as T?
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