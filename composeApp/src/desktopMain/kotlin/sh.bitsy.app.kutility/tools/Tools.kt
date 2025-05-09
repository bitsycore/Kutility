package sh.bitsy.app.kutility.tools

import androidx.compose.runtime.Composable
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.tools.encode.EncodeScreen
import sh.bitsy.app.kutility.tools.hash.HashScreen
import sh.bitsy.app.kutility.tools.json.JsonScreen

enum class Tools(val toolScreen: @Composable (appState: AppState) -> Unit, val enabled: Boolean = false) {
	HASH(@Composable { HashScreen(it) }, true),
	ENCODE(@Composable { EncodeScreen(it) }, true),
	JSON(@Composable { JsonScreen(it) }, true),
	UUID(@Composable { TodoToolScreen() }),
	FORMAT(@Composable { TodoToolScreen() }),
	CONVERT(@Composable { TodoToolScreen() }),
	COMPRESS(@Composable { TodoToolScreen() }),
	XML(@Composable { TodoToolScreen() }),
	URL(@Composable { TodoToolScreen() }),
	PASSWORD(@Composable { TodoToolScreen() }),
	REGEX(@Composable { TodoToolScreen() }),
	SORT(@Composable { TodoToolScreen() }),
}