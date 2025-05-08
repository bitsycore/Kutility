package sh.bitsy.app.kutility.tools.encoding

import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.rememberMenuState
import com.composeunstyled.Button
import com.composeunstyled.Checkbox
import com.composeunstyled.LocalContentColor
import com.composeunstyled.Text
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.ui.LocalAppTheme
import sh.bitsy.app.kutility.ui.TextField

@Composable
fun EncodingScreen(state: EncodingScreenState = remember { EncodingScreenState() }) {

	var inputText by state.inputText.collectAsMutableState()
	var outputText by state.outputText.collectAsMutableState()
	var selectedEncoding by state.selectedAlgorithm.collectAsMutableState()
	var selectedTextFormat by state.selectedTextFormat.collectAsMutableState()
	var autoConvert by state.autoConvert.collectAsMutableState()
	var lastChangedIsInput: Boolean by remember { mutableStateOf(false) }
	var screenSize by remember { mutableStateOf(DpSize.Zero) }
	val density = LocalDensity.current

	val performEncoding = {
		state.outputText.value = try {
			selectedEncoding.encoder.encode(inputText.toByteArray(selectedTextFormat.charset))
		} catch (e: Exception) {
			"Error: ${e.localizedMessage}"
		}
	}

	val performDecoding = {
		state.inputText.value = try {
			String(selectedEncoding.encoder.decode(outputText), selectedTextFormat.charset)
		} catch (e: Exception) {
			"Error: ${e.localizedMessage}"
		}
	}

	LaunchedEffect(inputText, outputText, selectedEncoding, selectedTextFormat, autoConvert) {
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
			.onGloballyPositioned {
				screenSize = DpSize(
					width = (it.size.width.toFloat() / density.density).dp,
					height = (it.size.height.toFloat() / density.density).dp
				)
			}
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	) {
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
			SelectedEncodingMenu({ selectedEncoding }) { selectedEncoding = it }
			SelectedTextFormatMenu({ selectedTextFormat }, { screenSize }) { selectedTextFormat = it }
			Row(verticalAlignment = Alignment.CenterVertically) {
				AutoConvertCheckbox({ autoConvert }) { autoConvert = it }
				EncodeDecodeButton(performEncoding, performDecoding) { autoConvert }
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

@Composable
private fun EncodeDecodeButton(performEncoding: () -> Unit, performDecoding: () -> Unit, autoConvert: () -> Boolean) {
	Button(
		onClick = performEncoding,
		borderColor = LocalAppTheme.current.borderColor,
		borderWidth = 1.dp,
		backgroundColor = if (autoConvert()) LocalAppTheme.current.disabledBgColor else LocalAppTheme.current.bg1Color,
		contentPadding = PaddingValues(8.dp),
		contentColor = if (autoConvert()) LocalAppTheme.current.disabledTextColor else LocalContentColor.current,
		shape = RoundedCornerShape(6.dp),
		modifier = Modifier.padding(end = 8.dp),
		enabled = !autoConvert()
	) {
		Text("Encode")
	}

	Button(
		onClick = performDecoding,
		borderColor = LocalAppTheme.current.borderColor,
		borderWidth = 1.dp,
		backgroundColor = if (autoConvert()) LocalAppTheme.current.disabledBgColor else LocalAppTheme.current.bg1Color,
		contentPadding = PaddingValues(8.dp),
		contentColor = if (autoConvert()) LocalAppTheme.current.disabledTextColor else LocalContentColor.current,
		shape = RoundedCornerShape(6.dp),
		enabled = !autoConvert()
	) {
		Text("Decode")
	}
}

@Composable
fun AutoConvertCheckbox(autoConvert: () -> Boolean, setAutoConvert: (Boolean) -> Unit) {
	Row {
		Text(
			text = "Auto Convert",
			textAlign = TextAlign.Center
		)
		Checkbox(
			checked = autoConvert(),
			onCheckedChange = { setAutoConvert(it) },
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
	}
}

@Composable
private fun SelectedTextFormatMenu(selectedTextFormat: () -> TextFormat, screenSize: () -> DpSize, setTextFormat: (TextFormat) -> Unit) {
	Menu(state = rememberMenuState(false)) {
		MenuButton(
			Modifier
				.clip(RoundedCornerShape(6.dp))
				.background(LocalAppTheme.current.bg1Color)
				.border(1.dp, LocalAppTheme.current.borderColor, RoundedCornerShape(6.dp))
		) {
			Text(
				text = "Text Format: ${selectedTextFormat().name}",
				modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
			)
		}
		MenuContent(
			modifier = Modifier.width(320.dp)
				.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
				.background(LocalAppTheme.current.bg1Color)
				.padding(4.dp)
				.heightIn(max = (screenSize().height * 0.5f).coerceIn(125.dp, 250.dp))
				.verticalScroll(rememberScrollState()),
			exit = fadeOut()
		) {
			TextFormat.available.first.forEachIndexed { index, option ->
				MenuItem(
					modifier = Modifier.clip(RoundedCornerShape(4.dp)).fillMaxWidth(),
					onClick = { setTextFormat(option) }
				) {
					Text(option.name)
				}
			}
				Box(
					modifier = Modifier.fillMaxWidth()
						.padding(12.dp)
						.height(2.dp)
						.background(LocalAppTheme.current.textColor.copy(alpha = 0.5f))
				)
			TextFormat.available.second.forEachIndexed { index, option ->
				MenuItem(
					modifier = Modifier.clip(RoundedCornerShape(4.dp)).fillMaxWidth(),
					onClick = { setTextFormat(option) }
				) {
					Text(option.name)
				}
			}
		}
	}
}

@Composable
private fun SelectedEncodingMenu(getEncoding: () -> EncodingFormat, setEncoding: (EncodingFormat) -> Unit) {
	Menu(state = rememberMenuState(false)) {
		MenuButton(
			Modifier
				.clip(RoundedCornerShape(6.dp))
				.background(LocalAppTheme.current.bg1Color)
				.border(1.dp, LocalAppTheme.current.borderColor, RoundedCornerShape(6.dp))
		) {
			Text(
				text = "Encoding: ${getEncoding().name}",
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
			EncodingFormat.entries.forEachIndexed { index, option ->
				MenuItem(
					modifier = Modifier.clip(RoundedCornerShape(4.dp)).fillMaxWidth(),
					onClick = { setEncoding(option) }
				) {
					Text(option.name)
				}
			}
		}
	}
}