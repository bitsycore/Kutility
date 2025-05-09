package sh.bitsy.app.kutility.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.tools.EncodeFormat
import sh.bitsy.app.kutility.tools.TextFormat
import sh.bitsy.app.kutility.ui.AutoConvertCheckbox
import sh.bitsy.app.kutility.ui.ButtonKuti
import sh.bitsy.app.kutility.ui.ContentKuti
import sh.bitsy.app.kutility.ui.ExpandedMenuKuti
import sh.bitsy.app.kutility.ui.MenuItemKuti
import sh.bitsy.app.kutility.ui.RowButtonKuti
import sh.bitsy.app.kutility.ui.SeparatorKuti
import sh.bitsy.app.kutility.ui.TextFieldKuti

@Composable
fun EncodeScreen(appState: AppState) {

	var inputText by remember { mutableStateOf("") }
	var outputText by remember { mutableStateOf("") }
	var selectedEncoding by remember { mutableStateOf(EncodeFormat.BASE64) }
	var selectedTextFormat by remember { mutableStateOf(TextFormat.Companion.default) }

	val autoConvert by appState.autoConvert.collectAsState()
	var lastChangedIsInput: Boolean by remember { mutableStateOf(false) }

	val performEncoding = {
		outputText = try {
			selectedEncoding.encoder.encode(inputText.toByteArray(selectedTextFormat.charset))
		} catch (e: Exception) {
			"Error: ${e.localizedMessage}"
		}
	}

	val performDecoding = {
		inputText = try {
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

	ContentKuti() {

		TextFieldKuti(
			placeHolder = "Decoded",
			textValue = { inputText },
			onTextChange = { inputText = it; lastChangedIsInput = true }
		)

		RowButtonKuti(Arrangement.SpaceBetween) {

			ExpandedMenuKuti("Encoding: ${selectedEncoding.name}") {
				EncodeFormat.entries.forEachIndexed { index, option ->
					MenuItemKuti(
						option.name,
						onClick = { selectedEncoding = option }
					)
				}
			}

			ExpandedMenuKuti("Text Format: ${selectedTextFormat.name}") {
				TextFormat.Companion.available.first.forEachIndexed { index, option ->
					MenuItemKuti(
						option.name,
						onClick = { selectedTextFormat = option }
					)
				}
				SeparatorKuti()
				TextFormat.Companion.available.second.forEachIndexed { index, option ->
					MenuItemKuti(
						option.name,
						onClick = { selectedTextFormat = option }
					)
				}
			}

			Row(verticalAlignment = Alignment.CenterVertically) {
				AutoConvertCheckbox({ autoConvert }) { appState.setAutoConvert(it) }
				ButtonKuti("Encode", performEncoding, autoConvert)
				ButtonKuti("Decode", performDecoding, autoConvert)
			}
		}

		TextFieldKuti(
			placeHolder = "Encoding",
			textValue = { outputText },
			onTextChange = { outputText = it; lastChangedIsInput = false }
		)
	}
}