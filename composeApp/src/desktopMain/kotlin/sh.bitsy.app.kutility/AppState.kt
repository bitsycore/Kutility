package sh.bitsy.app.kutility

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import sh.bitsy.app.kutility.local.LocalStorage
import sh.bitsy.app.kutility.tools.Tools
import sh.bitsy.app.kutility.ui.AppThemeType

class AppState {
    val selectedTool = MutableStateFlow(Tools.HASH)
    private val _themeType = MutableStateFlow(
        AppThemeType.valueOf(
            LocalStorage.getOrPut(
                "APP_THEME_TYPE",
                AppThemeType.SYSTEM.toString()
            )
        )
    )
    val themeType = _themeType.asStateFlow()
    fun setThemeType(themeType: AppThemeType) {
        _themeType.value = themeType
        LocalStorage.put("APP_THEME_TYPE", themeType.toString())
    }
}