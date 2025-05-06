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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import kutility.composeapp.generated.resources.Res
import kutility.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.tools.Tools
import sh.bitsy.app.kutility.ui.AppTheme
import sh.bitsy.app.kutility.ui.LocalAppTheme
import sh.bitsy.app.kutility.ui.ProvideTheme
import sh.bitsy.app.kutility.ui.diagonalPattern
import kotlin.time.Duration.Companion.seconds

fun main() = application {
	Window(
		onCloseRequest = ::exitApplication,
		title = "Kutility",
		icon = painterResource(Res.drawable.compose_multiplatform),
	) {
		val theme = if (isSystemInDarkTheme()) AppTheme.darkTheme else AppTheme.lightTheme
		ProvideTheme(theme) {
			Column(
				Modifier.fillMaxWidth().wrapContentHeight().diagonalPattern(
					color1 = LocalAppTheme.current.bg1Color,
					color2 = LocalAppTheme.current.bg2Color,
					stripeWidth = LocalAppTheme.current.bgStripWidth,
				)
			) {
				Row(Modifier.background(LocalAppTheme.current.grayColor).fillMaxWidth()) {
					Button(
						onClick = { println("TODO") },
						contentPadding = PaddingValues(8.dp)
					) {
						Text("File")
					}
					Button(
						onClick = { println("TODO") },
						contentPadding = PaddingValues(8.dp)
					) {
						Text("Edit")
					}
				}
				Row {
					val appState = remember { AppState() }
					ToolsList(appState)
					CurrentTool(appState)
				}
			}
		}

	}
}


@Composable
private fun ToolsList(
	appState: AppState
) {
	val appTheme = LocalAppTheme.current
	val lazyListState = rememberLazyListState()
	val state = rememberScrollAreaState(lazyListState)
	var currentTool by appState.currentTool.collectAsMutableState()
	ScrollArea(state = state, Modifier.wrapContentWidth()) {
		LazyColumn(
			state = lazyListState,
			modifier = Modifier.width(100.dp)
		) {
			Tools.entries.forEach { tool ->
				item(key = tool) {
					val isSelected = currentTool == tool
					Button(
						onClick = { currentTool = tool },
						contentColor = if (!isSelected) appTheme.textColor else appTheme.textColor,
						backgroundColor = if (!isSelected) appTheme.grayColor else appTheme.bg2Color,
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
				Modifier.background(LocalAppTheme.current.textColor),
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
	val currentTool by appState.currentTool.collectAsState()
	currentTool.toolScreen(appState)
}