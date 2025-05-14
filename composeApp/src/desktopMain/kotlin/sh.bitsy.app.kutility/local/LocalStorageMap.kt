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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.reflect.KType
import kotlin.reflect.typeOf

abstract class LocalStorageMap(val filename: String, val path: File) {
	private val coroutineScope = CoroutineScope(Dispatchers.IO)
	private val accessMapMutex = Mutex()
	private val fileSaveMutex = Mutex()
	private val jsonSerializer = Json { prettyPrint = true; ignoreUnknownKeys = true; explicitNulls = false }
	private var mapCache: MutableMap<String, JsonElement>? = null
	private var pendingSaveJob: Job? = null

	private val file: File by lazy {
		File(path, "$filename.json")
	}



	private suspend fun loadMapFromFile(): MutableMap<String, JsonElement> {
		return mapCache ?: withContext(Dispatchers.IO) {

			fun loadFromFile(targetFile: File): MutableMap<String, JsonElement>? = try {
				if (!targetFile.exists() || targetFile.length() == 0L) null
				else {
					val content = targetFile.readText()
					if (content.isBlank()) null
					else jsonSerializer.decodeFromString<Map<String, JsonElement>>(content).toMutableMap()
				}
			} catch (e: IOException) {
				System.err.println("Error reading settings from $targetFile: ${e.message}")
				null
			} catch (e: SerializationException) {
				System.err.println("Error deserializing settings from $targetFile (possibly corrupted): ${e.message}")
				val corruptedFile = File("${targetFile.absolutePath}.corrupted.${System.currentTimeMillis()}")
				targetFile.renameTo(corruptedFile)
				null
			}

			val mainFile = file
			val backupFile = File("${file.absolutePath}.bak")

			// Try main file
			val loadedMap = loadFromFile(mainFile)

			// Try backup if failed
			val finalMap = if (loadedMap == null && backupFile.exists()) {
				loadFromFile(backupFile)
			} else {
				loadedMap
			}

			return@withContext (finalMap ?: mutableMapOf()).also {
				mapCache = it
			}
		}
	}

	private suspend fun saveMapToFile() {
		val currentMapToSave = mapCache?.toMap() ?: return
		fileSaveMutex.withLock {
			try {
				val tempFile = File("${file.absolutePath}.tmp")
				val bakFile = File("${file.absolutePath}.bak")
				val jsonString = jsonSerializer.encodeToString(currentMapToSave)
				tempFile.parentFile?.mkdirs()
				tempFile.outputStream().use { output ->
					output.write(jsonString.toByteArray())
					output.flush()
					output.fd.sync()
				}
				if (file.exists()) {
					Files.move(file.toPath(), bakFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
				}
				Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE)
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
	): T = accessMapMutex.withLock {
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
		accessMapMutex.withLock {
			pendingSaveJob?.join()
			pendingSaveJob = null
		}
	}
}