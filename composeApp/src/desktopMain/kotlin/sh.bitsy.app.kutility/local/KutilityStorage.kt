package sh.bitsy.app.kutility.local

private val KutilityPathProvider = SafeUserPathProvider("sh.bitsy", "kutility")

object SettingsStorage : LocalStorage("settings", KutilityPathProvider.dir)

suspend fun flushAllStorages() = SettingsStorage.flush()