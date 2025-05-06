package sh.bitsy.app.kutility.tools

import androidx.compose.runtime.Composable
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.tools.cipher.CipherScreen
import sh.bitsy.app.kutility.tools.encoding.EncodingScreen
import sh.bitsy.app.kutility.tools.hash.HashScreen

enum class Tools(val toolScreen: @Composable (appState: AppState) -> Unit, val enabled: Boolean = false, ) {
    HASH(@Composable { HashScreen(it) }, true),
    ENCODING(@Composable { EncodingScreen(it) }, true),
    UUID(@Composable { CipherScreen(it) }),
    FORMAT(@Composable { CipherScreen(it) }),
    JSON(@Composable { CipherScreen(it) }),
    XML(@Composable { CipherScreen(it) }),
    BASE64(@Composable { CipherScreen(it) }),
    BASE45(@Composable { CipherScreen(it) }),
    URL(@Composable { CipherScreen(it) }),
    PASSWORD(@Composable { CipherScreen(it) }),
    REGEX(@Composable { CipherScreen(it) }),
    HEX(@Composable { CipherScreen(it) }),
    SORT(@Composable { CipherScreen(it) }),
}