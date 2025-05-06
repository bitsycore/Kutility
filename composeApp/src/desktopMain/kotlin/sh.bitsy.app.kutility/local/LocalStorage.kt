package sh.bitsy.app.kutility.local

import org.mapdb.DBMaker
import org.mapdb.Serializer
import java.io.File
import java.nio.file.Paths.get


object LocalStorage {
    private const val DB_NAME = "kutility.db"
    private const val DB_KEY = "settings"

    private val fullPath: String by lazy {
        val os = System.getProperty("os.name").lowercase()
        val appData = when {
            os.contains("win") -> System.getenv("APPDATA") ?: System.getProperty("user.home")
            os.contains("mac") -> get(
                System.getProperty("user.home"),
                "Library",
                "Application Support"
            ).toString()
            os.contains("nix") || os.contains("nux") -> get(
                System.getProperty("user.home"),
                ".config"
            ).toString()
            else -> System.getProperty("user.home")
        }
        val path = "${appData}${File.separator}sh.bitsy${File.separator}kutility${File.separator}$DB_NAME"
        val file = File(path)

        file.parentFile?.mkdirs()

        return@lazy path
    }

    private inline fun <T> settings(block: MutableMap<String, String>.() -> T): T {
        val db = DBMaker.fileDB(fullPath).make()
        val map = db.hashMap<String, String>(DB_KEY, Serializer.STRING, Serializer.STRING)
            .keySerializer(Serializer.STRING)
            .valueSerializer(Serializer.STRING)
            .createOrOpen()
        val result = block(map)
        db.close()
        return result
    }

    fun get(key: String): String = settings {
        this[key] ?: throw IllegalArgumentException("Key $key not found in local storage")
    }

    fun getOrPut(key: String, value: String): String = settings {
        getOrPut(key) { value }
    }

    fun getOrNull(key: String): String? = settings {
        this[key]
    }

    fun getOrDefault(key: String, defaultValue: String): String = settings {
        getOrDefault(key, defaultValue)
    }

    fun remove(key: String) = settings {
        remove(key)
    }

    fun clear() = settings {
        clear()
    }

    fun contains(key: String): Boolean = settings {
        containsKey(key)
    }

    fun getAll(): Map<String, String> = settings {
        toMap()
    }

    fun put(key: String, value: String) = settings {
        put(key, value)
    }
}