package sh.bitsy.app.kutility.tools

import androidx.compose.runtime.Composable
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.tools.hash.HashScreen
import sh.bitsy.app.kutility.tools.cipher.CipherScreen

enum class Tools(val toolScreen: @Composable (appState: AppState) -> Unit) {
    HASH(@Composable { HashScreen(it) }),
    CIPHER(@Composable { CipherScreen(it) }),
    UUID(@Composable { CipherScreen(it) }),
    FORMAT(@Composable { CipherScreen(it) }),
    JSON(@Composable { CipherScreen(it) }),
    XML(@Composable { CipherScreen(it) }),
}