package sh.bitsy.app.kutility.local

import java.io.File

class SafeUserPathProvider(private val companyName: String, private val appName: String) {

	private enum class OS { WINDOWS, MACOS, LINUX_OR_OTHER }

	val absolutePath: String by lazy { dir.absolutePath }

	private fun ensureDirectoryExists(dir: File): Boolean {
		return if (dir.exists()) {
			dir.isDirectory
		} else {
			dir.mkdirs()
		}
	}

	private val currentOS: OS by lazy {
		val osName = System.getProperty("os.name", "unknown").lowercase()
		when {
			osName.contains("win") -> OS.WINDOWS
			osName.contains("mac") -> OS.MACOS
			else -> OS.LINUX_OR_OTHER
		}
	}

	val dir: File by lazy {
		val userHome = System.getProperty("user.home", ".")
		val baseDir: File = when (currentOS) {
			OS.WINDOWS -> {
				val appData = System.getenv("APPDATA")
				if (appData != null && appData.isNotEmpty()) {
					File(appData)
				} else {
					File(userHome, "AppData").resolve("Roaming")
				}
			}
			OS.MACOS -> File(userHome, "Library").resolve("Application Support")
			OS.LINUX_OR_OTHER -> {
				val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
				if (xdgConfigHome != null && xdgConfigHome.isNotEmpty()) {
					File(xdgConfigHome)
				} else {
					File(userHome, ".config")
				}
			}
		}

		baseDir.resolve(companyName).resolve(appName).also {
			ensureDirectoryExists(it)
		}
	}
}