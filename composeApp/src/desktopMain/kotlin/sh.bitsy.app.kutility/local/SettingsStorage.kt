package sh.bitsy.app.kutility.local

object SettingsStorage : LocalStorage() {
    override val fileName: String = "kutility.settings.json"
}