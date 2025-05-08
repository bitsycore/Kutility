@file:OptIn(ExperimentalEncodingApi::class)

package sh.bitsy.app.kutility.tools.encoding

import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.MenuState
import com.composeunstyled.Button
import com.composeunstyled.Checkbox
import com.composeunstyled.LocalContentColor
import com.composeunstyled.Text
import kotlinx.coroutines.flow.MutableStateFlow
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.ui.LocalAppTheme
import sh.bitsy.app.kutility.ui.TextField
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

enum class Encoding {
	BASE64,
	BASE45
}

data class EncodingScreenState(
	val inputText: MutableStateFlow<String> = MutableStateFlow(""),
	val outputText: MutableStateFlow<String> = MutableStateFlow(""),
	val selectedAlgorithm: MutableStateFlow<Encoding> = MutableStateFlow(Encoding.BASE64),
	val autoConvert: MutableStateFlow<Boolean> = MutableStateFlow(false),
	val dropdownState: MenuState = MenuState(expanded = false)
)

@Composable
fun EncodingScreen(appState: AppState) {

	val state = remember { EncodingScreenState() }

	var inputText by state.inputText.collectAsMutableState()
	var outputText by state.outputText.collectAsMutableState()
	var selectedEncoding by state.selectedAlgorithm.collectAsMutableState()
	var autoConvert by state.autoConvert.collectAsMutableState()

	var lastChangedIsInput: Boolean by remember { mutableStateOf(false) }

	val performEncoding = {
		state.outputText.value = try {
			when (selectedEncoding) {
				Encoding.BASE64 -> Base64.encode(inputText.toByteArray())
				Encoding.BASE45 -> Base45.encode(inputText.toByteArray())
			}
		} catch (e: Exception) {
			"Error: ${e.localizedMessage}"
		}
	}

	val performDecoding = {
		state.inputText.value = try {
			String(
				when (selectedEncoding) {
					Encoding.BASE64 -> Base64.decode(outputText)
					Encoding.BASE45 -> Base45.decode(outputText)
				}
			)
		} catch (e: Exception) {
			"Error: ${e.localizedMessage}"
		}
	}

	LaunchedEffect(inputText, outputText, selectedEncoding, autoConvert) {
		if (autoConvert) {
			if (lastChangedIsInput)
				performEncoding()
			else
				performDecoding()
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	)
	{
		Text("Encoding Tool")

		TextField(
			value = inputText,
			onValueChange = { inputText = it; lastChangedIsInput = true },
			placeholder = "Decoded",
			borderColor = LocalAppTheme.current.borderColor,
			modifier = Modifier.fillMaxWidth().heightIn(150.dp),
			singleLine = false,
			contentPadding = PaddingValues(8.dp),
			backgroundColor = LocalAppTheme.current.bg1Color,
			shape = RoundedCornerShape(8.dp),
			textAlign = TextAlign.Start
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Menu(
				state = state.dropdownState
			) {
				MenuButton(
					Modifier
						.clip(RoundedCornerShape(6.dp))
						.background(LocalAppTheme.current.bg1Color)
						.border(1.dp, LocalAppTheme.current.borderColor, RoundedCornerShape(6.dp))
				) {
					Text(
						text = "Encoding: ${selectedEncoding.name}",
						modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
					)
				}
				MenuContent(
					modifier = Modifier.width(320.dp)
						.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
						.background(LocalAppTheme.current.bg1Color)
						.padding(4.dp),
					exit = fadeOut()
				) {
					Encoding.entries.forEachIndexed { index, option ->
						MenuItem(
							modifier = Modifier.clip(RoundedCornerShape(4.dp)).fillMaxWidth(),
							onClick = { selectedEncoding = option }
						) {
							Text(option.name)
						}
					}
				}
			}

			Row(verticalAlignment = Alignment.CenterVertically) {
				Text(
					text = "Auto Convert",
					textAlign = TextAlign.Center
				)
				Checkbox(
					checked = autoConvert,
					onCheckedChange = { autoConvert = it },
					shape = RoundedCornerShape(4.dp),
					backgroundColor = LocalAppTheme.current.bg1Color,
					borderWidth = 1.dp,
					borderColor = LocalAppTheme.current.borderColor,
					modifier = Modifier.padding(horizontal = 8.dp).size(24.dp),
					contentDescription = "Auto Convert"
				) {
					Text(
						text = "✓",
						fontSize = 16.sp,
						textAlign = TextAlign.Center,
						modifier = Modifier.fillMaxSize()
							.wrapContentHeight(align = Alignment.CenterVertically)
							.padding(2.dp)
					)
				}

				Button(
					onClick = performEncoding,
					borderColor = LocalAppTheme.current.borderColor,
					borderWidth = 1.dp,
					backgroundColor = if (autoConvert) LocalAppTheme.current.disabledBgColor else LocalAppTheme.current.bg1Color,
					contentPadding = PaddingValues(8.dp),
					contentColor = if (autoConvert) LocalAppTheme.current.disabledTextColor else LocalContentColor.current,
					shape = RoundedCornerShape(6.dp),
					modifier = Modifier.padding(end = 8.dp),
					enabled = !autoConvert
				) {
					Text("Encode")
				}

				Button(
					onClick = performDecoding,
					borderColor = LocalAppTheme.current.borderColor,
					borderWidth = 1.dp,
					backgroundColor = if (autoConvert) LocalAppTheme.current.disabledBgColor else LocalAppTheme.current.bg1Color,
					contentPadding = PaddingValues(8.dp),
					contentColor = if (autoConvert) LocalAppTheme.current.disabledTextColor else LocalContentColor.current,
					shape = RoundedCornerShape(6.dp),
					enabled = !autoConvert
				) {
					Text("Decode")
				}
			}
		}

		TextField(
			value = outputText,
			onValueChange = { outputText = it; lastChangedIsInput = false },
			placeholder = "Encoded",
			borderColor = LocalAppTheme.current.borderColor,
			contentPadding = PaddingValues(8.dp),
			backgroundColor = LocalAppTheme.current.bg1Color,
			modifier = Modifier.fillMaxWidth().heightIn(150.dp),
			editable = true,
			singleLine = false,
			shape = RoundedCornerShape(8.dp),
		)
	}
}