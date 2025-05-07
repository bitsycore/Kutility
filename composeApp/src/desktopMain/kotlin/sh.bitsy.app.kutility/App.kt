package sh.bitsy.app.kutility

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
import sh.bitsy.app.kutility.tools.Tools
import sh.bitsy.app.kutility.ui.AppTheme
import sh.bitsy.app.kutility.ui.AppThemeType
import sh.bitsy.app.kutility.ui.LocalAppTheme
import sh.bitsy.app.kutility.ui.ProvideTheme
import sh.bitsy.app.kutility.ui.diagonalPattern
import kotlin.time.Duration.Companion.seconds

val coroutineScope = CoroutineScope(Dispatchers.IO)

fun main() = application {
    Window(
        onCloseRequest = {
            coroutineScope.launch {
                flushAllStorages()
                exitApplication()
            }
        },
        title = "Kutility",
        icon = painterResource(Res.drawable.compose_multiplatform),
    ) {
        val appState = remember { AppState() }
        val currentThemeType by appState.themeType.collectAsState()
        ProvideTheme(
            when (currentThemeType) {
                AppThemeType.SYSTEM -> if (isSystemInDarkTheme()) AppTheme.DARK else AppTheme.LIGHT
                AppThemeType.LIGHT -> AppTheme.LIGHT
                AppThemeType.DARK -> AppTheme.DARK
            }
        ) {
            val theme = LocalAppTheme.current
            Column(
                Modifier.fillMaxWidth().diagonalPattern(
                    color1 = theme.bg1Color,
                    color2 = theme.bg2Color,
                    stripeWidth = theme.bgStripWidth,
                )
            ) {
                Row(Modifier.background(theme.borderColor).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    MenuBar(appState)
                }
                Row {
                    ToolsList(appState)
                    CurrentTool(appState)
                }
            }
        }

    }
}

@Composable
private fun MenuBar(appState: AppState) {
    val appTheme = LocalAppTheme.current
    Button(
        onClick = {  },
        enabled = false,
        backgroundColor = appTheme.borderColor,
        contentPadding = PaddingValues(8.dp)
    ) {
        Text("Theme")
    }
	AppThemeType.entries.forEach { themeType ->
		Button(
			onClick = { appState.setThemeType(themeType) },
			contentPadding = PaddingValues(8.dp),
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            backgroundColor = appTheme.bg2Color.copy(alpha = 0.5f),
            shape = appTheme.buttonShape,
		) {
			Text(themeType.name.lowercase().replaceFirstChar { it.uppercase() })
		}
	}
}


@Composable
private fun ToolsList(appState: AppState) {
    val appTheme = LocalAppTheme.current
    val lazyListState = rememberLazyListState()
    val state = rememberScrollAreaState(lazyListState)
    var currentTool by appState.selectedTool.collectAsMutableState()
    ScrollArea(state = state, Modifier.wrapContentWidth()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.width(100.dp)
        ) {
            Tools.entries.forEach { tool ->
                item(key = tool) {
                    val isSelected = currentTool == tool
                    Button(
                        enabled = !tool.enabled.not(),
                        onClick = { currentTool = tool },
                        contentColor = when {
                            tool.enabled.not() -> appTheme.disabledTextColor
                            !isSelected -> appTheme.textColor
                            else -> appTheme.textColor
                        },
                        backgroundColor = when {
                            tool.enabled.not() -> appTheme.disabledBgColor
                            !isSelected -> appTheme.grayColor
                            else -> appTheme.bg2Color
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                            .drawWithContent {
                                drawContent()
                                val thickness = 1.dp.toPx()
                                drawLine(
                                    color = appTheme.borderColor,
                                    start = Offset(0f, size.height - thickness / 2),
                                    end = Offset(size.width, size.height - thickness / 2),
                                    strokeWidth = thickness
                                )
                            }
                    ) {
                        Text(tool.name.lowercase().replaceFirstChar { it.uppercase() })
                    }
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
fun CurrentTool(appState: AppState) {
    val currentTool by appState.selectedTool.collectAsState()
    currentTool.toolScreen(appState)
}