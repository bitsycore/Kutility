package sh.bitsy.app.kutility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import sh.bitsy.app.kutility.tools.Tools

@Composable
fun rememberAppState() = remember { AppState() }

data class AppState(
    val currentTool: MutableStateFlow<Tools> = MutableStateFlow(Tools.HASH),
)