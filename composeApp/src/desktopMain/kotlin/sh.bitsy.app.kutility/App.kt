package sh.bitsy.app.kutility

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
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
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.tools.Tools
import kotlin.time.Duration

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kutility",
    ) {
        Row(
            Modifier.diagonalPattern(
                color1 = Color(0xFFFFFFFF),
                color2 = Color(0xFFF0F0F0),
                stripeWidth = 3.dp,
            )
        ) {
            val appState = rememberAppState()
            ToolsList(appState)
            CurrentTool(appState)
        }
    }
}


@Composable
private fun ToolsList(
    appState: AppState
) {
    val lazyListState = rememberLazyListState()
    val state = rememberScrollAreaState(lazyListState)
    var currentTool by appState.currentTool.collectAsMutableState()
    ScrollArea(state = state, Modifier.wrapContentWidth().background(Color.Black.copy(alpha = 0.80f))) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.width(120.dp)
        ) {
            Tools.entries.forEach { tool ->
                item(key = tool) {
                    val isSelected = currentTool == tool
                    Button(
                        onClick = { currentTool = tool },
                        contentColor = if (!isSelected) Color.White else Color.Black,
                        backgroundColor = if(!isSelected) Color.DarkGray else Color.LightGray,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        modifier = Modifier.fillMaxWidth()
                            .drawWithContent {
                                drawContent()
                                val thickness = 1.dp.toPx()
                                drawLine(
                                    color = Color.Black,
                                    start = Offset(0f, size.height - thickness / 2),
                                    end = Offset(size.width, size.height - thickness / 2),
                                    strokeWidth = thickness
                                )
                            }
                    ) {
                        Text(tool.name)
                    }
                }
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.TopEnd)
                .fillMaxHeight()
                .width(8.dp)
        ) {
            Thumb(
                Modifier.background(Color.LightGray),
                ThumbVisibility.HideWhileIdle(
                    EnterTransition.None,
                    ExitTransition.None,
                    Duration.ZERO
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