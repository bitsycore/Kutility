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
			val themeType = SettingsStorage.getStringOrPut("APP_THEME_TYPE", AppThemeType.SYSTEM.toString())
			_themeType.value = AppThemeType.valueOf(themeType)
		}
	}

	val selectedTool = MutableStateFlow(Tools.HASH)
	private val _themeType = MutableStateFlow(AppThemeType.SYSTEM)
	val themeType = _themeType.asStateFlow()
	fun setThemeType(themeType: AppThemeType) {
		_themeType.value = themeType
		coroutineScope.launch {
			SettingsStorage.putString("APP_THEME_TYPE", themeType.toString())
		}
	}
}