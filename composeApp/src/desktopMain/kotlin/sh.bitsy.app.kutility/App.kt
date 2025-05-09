package sh.bitsy.app.kutility

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.composables.core.HorizontalSeparator
import com.composables.core.ScrollArea
import com.composables.core.Thumb
import com.composables.core.ThumbVisibility
import com.composables.core.VerticalScrollbar
import com.composables.core.rememberScrollAreaState
import com.composeunstyled.Button
import com.composeunstyled.Text
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kutility.composeapp.generated.resources.Res
import kutility.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.local.flushAllStorages
import sh.bitsy.app.kutility.tools.ToolsType
import sh.bitsy.app.kutility.ui.AppThemeType
import sh.bitsy.app.kutility.ui.LocalAppTheme
import sh.bitsy.app.kutility.ui.ProvideTheme
import sh.bitsy.app.kutility.ui.diagonalPattern
import sh.bitsy.app.kutility.ui.theme
import kotlin.time.Duration.Companion.seconds

val coroutineScope = CoroutineScope(Dispatchers.IO)

fun main() = application {

	var alreadyLaunched = false
	val cleanAndExitApp: () -> Unit = l@{
		if (alreadyLaunched) return@l
		alreadyLaunched = true
		coroutineScope.launch {
			flushAllStorages()
			exitApplication()
		}
	}

	Window(
		onCloseRequest = cleanAndExitApp,
		title = "Kutility",
		icon = painterResource(Res.drawable.compose_multiplatform),
	) {
		val appState = remember { AppState() }
		val currentThemeType by appState.themeType.collectAsState()
		val autoConvert by appState.autoConvert.collectAsState()
		TopMenu(cleanAndExitApp, autoConvert, appState, currentThemeType)
		ProvideTheme(currentThemeType.theme) { theme ->
			Row(
				Modifier.fillMaxWidth().diagonalPattern(
					color1 = theme.bg1Color,
					color2 = theme.bg2Color,
					stripeWidth = theme.bgStripWidth,
				)
			) {
				LeftToolList(appState)
				RightCurrentTool(appState)
			}
		}

	}
}

@Composable
private fun FrameWindowScope.TopMenu(
	cleanAndExitApp: () -> Unit,
	autoConvert: Boolean,
	appState: AppState,
	currentThemeType: AppThemeType
) {
	MenuBar {
		Menu("File", 'F') {
			Item("Exit", mnemonic = 'Q', onClick = cleanAndExitApp)
		}
		Menu("Settings", 'S') {
			Item("Auto by default", mnemonic = 'A', icon = if (autoConvert) painterResource(Res.drawable.compose_multiplatform) else null) {
				appState.setAutoConvert(!autoConvert)
			}
			Menu("Theme", mnemonic = 'T') {
				AppThemeType.entries.forEach { themeType ->
					Item(
						text = themeType.name.lowercase().replaceFirstChar { it.uppercase() },
						mnemonic = themeType.name.first(),
						icon = if (themeType == currentThemeType) painterResource(Res.drawable.compose_multiplatform) else null,
					) {
						appState.setThemeType(themeType)
					}
				}
			}
		}
	}
}

@Composable
private fun LeftToolList(appState: AppState) {
	val appTheme = LocalAppTheme.current
	val lazyListState = rememberLazyListState()
	val state = rememberScrollAreaState(lazyListState)
	var currentTool by appState.selectedTool.collectAsMutableState()
	ScrollArea(state = state, Modifier.wrapContentWidth()) {
		LazyColumn(
			state = lazyListState,
			modifier = Modifier.width(100.dp)
				.padding(8.dp)
				.border(1.dp, appTheme.borderColor, shape = RoundedCornerShape(8.dp))
				.shadow(1.dp, shape = RoundedCornerShape(8.dp))
		) {
			ToolsType.entries.forEach { tool ->
				item(key = tool) {
					Button(
						enabled = tool.enabled,
						onClick = { currentTool = tool },
						contentColor = when {
							!tool.enabled -> appTheme.disabledTextColor
							currentTool != tool -> appTheme.textColor
							else -> appTheme.selectedTextColor
						},
						backgroundColor = when {
							!tool.enabled -> appTheme.disabledBgColor
							currentTool != tool -> appTheme.bg1Color
							else -> appTheme.selectedBgColor
						},
						borderColor = when {
							!tool.enabled -> appTheme.disabledBgColor
							currentTool != tool -> appTheme.borderColor
							else -> appTheme.selectedBorderColor
						},
						borderWidth = 1.dp,
						contentPadding = PaddingValues(horizontal = 0.dp, vertical = 12.dp),
						modifier = Modifier.fillMaxWidth()
					) {
						Text(tool.name.lowercase().replaceFirstChar { it.uppercase() })
					}
					HorizontalSeparator(appTheme.borderColor)
				}
			}
		}
		VerticalScrollbar(
			modifier = Modifier.align(Alignment.TopEnd)
				.fillMaxHeight()
				.width(4.dp)
		) {
			Thumb(
				Modifier.background(appTheme.textColor),
				ThumbVisibility.HideWhileIdle(
					fadeIn(),
					fadeOut(),
					0.5.seconds,
				)
			)
		}
	}
}

@Composable
fun RightCurrentTool(appState: AppState) {
	val currentTool by appState.selectedTool.collectAsState()
	AnimatedContent(currentTool) {
		it.toolScreen(appState)
	}
}