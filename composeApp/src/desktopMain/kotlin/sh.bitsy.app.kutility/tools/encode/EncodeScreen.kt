@file:JvmName("EncodeScreenStateKt")

package sh.bitsy.app.kutility.tools.encode

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
import androidx.compose.ui.unit.DpSize
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.tools.AutoConvertCheckbox
import sh.bitsy.app.kutility.tools.ButtonKuti
import sh.bitsy.app.kutility.tools.ContentKuti
import sh.bitsy.app.kutility.tools.ExpandedMenuKuti
import sh.bitsy.app.kutility.tools.MenuItemKuti
import sh.bitsy.app.kutility.tools.RowButtonKuti
import sh.bitsy.app.kutility.tools.SeparatorKuti
import sh.bitsy.app.kutility.tools.TextFieldKuti

@Composable
fun EncodeScreen(appState: AppState, state: EncodingScreenState = remember { EncodingScreenState() }) {

	var inputText by state.inputText.collectAsMutableState()
	var outputText by state.outputText.collectAsMutableState()
	var selectedEncoding by state.selectedAlgorithm.collectAsMutableState()
	var selectedTextFormat by state.selectedTextFormat.collectAsMutableState()
	val autoConvert by appState.autoConvert.collectAsState()
	var lastChangedIsInput: Boolean by remember { mutableStateOf(false) }
	var screenSize by remember { mutableStateOf(DpSize.Zero) }

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

	ContentKuti(appState) {

		TextFieldKuti(
			placeHolder = "Decoded",
			textValue = { inputText },
			onTextChange = { inputText = it; lastChangedIsInput = true }
		)

		RowButtonKuti(Arrangement.SpaceBetween) {

			ExpandedMenuKuti("Encoding: ${selectedEncoding.name}") {
				EncodingFormat.entries.forEachIndexed { index, option ->
					MenuItemKuti(
						option.name,
						onClick = { selectedEncoding = option }
					)
				}
			}

			ExpandedMenuKuti("Text Format: ${selectedTextFormat.name}") {
				TextFormat.available.first.forEachIndexed { index, option ->
					MenuItemKuti(
						option.name,
						onClick = { selectedTextFormat = option }
					)
				}
				SeparatorKuti()
				TextFormat.available.second.forEachIndexed { index, option ->
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
			textValue = { selectedEncoding.name },
			onTextChange = { outputText = it; lastChangedIsInput = false }
		)
	}
}