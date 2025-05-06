package sh.bitsy.app.kutility

import kotlinx.coroutines.flow.MutableStateFlow
import sh.bitsy.app.kutility.tools.Tools

data class AppState(
    val currentTool: MutableStateFlow<Tools> = MutableStateFlow(Tools.HASH),
)