package sh.bitsy.app.kutility

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import sh.bitsy.app.kutility.local.SettingsStorage
import sh.bitsy.app.kutility.tools.Tools
import sh.bitsy.app.kutility.ui.AppThemeType

class AppState {
	private val coroutineScope = CoroutineScope(Dispatchers.IO)

	init {
		coroutineScope.launch(Dispatchers.IO) {
			val themeType = SettingsStorage.getOrPut<AppThemeType>("APP_THEME_TYPE", AppThemeType.SYSTEM)
			_themeType.value = themeType
			_autoConvert.value = SettingsStorage.getOrPut<Boolean>("AUTO_CONVERT", false)
		}
	}

	val selectedTool = MutableStateFlow(Tools.HASH)
	private val _themeType = MutableStateFlow(AppThemeType.SYSTEM)
	val themeType = _themeType.asStateFlow()
	private val _autoConvert = MutableStateFlow(false)
	val autoConvert = _autoConvert.asStateFlow()
	fun setThemeType(themeType: AppThemeType) {
		_themeType.value = themeType
		coroutineScope.launch {
			SettingsStorage.put("APP_THEME_TYPE", themeType)
		}
	}

	fun setAutoConvert(autoConvert: Boolean) {
		_autoConvert.value = autoConvert
		coroutineScope.launch {
			SettingsStorage.put<Boolean>("AUTO_CONVERT", autoConvert)
		}
	}
}