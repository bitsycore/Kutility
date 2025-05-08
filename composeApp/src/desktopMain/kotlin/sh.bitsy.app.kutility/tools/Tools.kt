package sh.bitsy.app.kutility.tools

import androidx.compose.runtime.Composable
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.tools.encoding.EncodingScreen
import sh.bitsy.app.kutility.tools.hash.HashScreen

enum class Tools(val toolScreen: @Composable (appState: AppState) -> Unit, val enabled: Boolean = false) {
	HASH(@Composable { HashScreen(it) }, true),
	ENCODING(@Composable { EncodingScreen(it) }, true),
	UUID(@Composable { TodoToolScreen() }),
	FORMAT(@Composable { TodoToolScreen() }),
	JSON(@Composable { TodoToolScreen() }),
	XML(@Composable { TodoToolScreen() }),
	BASE64(@Composable { TodoToolScreen() }),
	BASE45(@Composable { TodoToolScreen() }),
	URL(@Composable { TodoToolScreen() }),
	PASSWORD(@Composable { TodoToolScreen() }),
	REGEX(@Composable { TodoToolScreen() }),
	HEX(@Composable { TodoToolScreen() }),
	SORT(@Composable { TodoToolScreen() }),
}