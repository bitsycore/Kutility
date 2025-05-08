package sh.bitsy.app.kutility.local

abstract class KutilityStorage : LocalStorage() {
	override val companyName: String = "sh.bitsy"
	override val appName: String = "kutility"
}

object SettingsStorage : KutilityStorage() {
	override val fileName: String = "settings"
}

suspend fun flushAllStorages() = SettingsStorage.flush()