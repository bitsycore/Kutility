package sh.bitsy.app.kutility.local

private val KutilityPathProvider = UserPathProvider("sh.bitsy", "kutility")

object SettingsStorage : LocalStorageMap("settings", KutilityPathProvider.dir)

suspend fun flushAllStorages() = SettingsStorage.flush()