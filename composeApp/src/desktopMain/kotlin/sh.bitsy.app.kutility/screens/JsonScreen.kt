package sh.bitsy.app.kutility.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.ui.AutoConvertCheckbox
import sh.bitsy.app.kutility.ui.ButtonKuti
import sh.bitsy.app.kutility.ui.ContentKuti
import sh.bitsy.app.kutility.ui.RowButtonKuti
import sh.bitsy.app.kutility.ui.TextFieldKuti

private val PRETTIFY_JSON = Json { prettyPrint = true }
private val MINIFY_JSON = Json { prettyPrint = false }

@Composable
fun JsonScreen(appState: AppState) {
	var inputText by remember { mutableStateOf("") }
	var outputText by remember { mutableStateOf("") }
	var lastChangedIsInput: Boolean by remember { mutableStateOf(false) }

	val autoConvert by appState.autoConvert.collectAsState()

	val minify = {
		if (inputText.isNotEmpty()) {
			try {
				val json = MINIFY_JSON.parseToJsonElement(inputText)
				outputText = json.jsonObject.toString()
			} catch (e: Exception) {
				outputText = "Error: ${e.message}"
			}
		}
	}

	val prettify = {
		if (outputText.isNotEmpty()) {
			try {
				val json = PRETTIFY_JSON.encodeToString<JsonElement>(PRETTIFY_JSON.decodeFromString<JsonElement>(outputText))
				inputText = json
			} catch (e: Exception) {
				inputText = "Error: ${e.message}"
			}
		}
	}

	LaunchedEffect(inputText, outputText, autoConvert) {
		if (autoConvert) {
			if (lastChangedIsInput) {
				minify()
			} else {
				prettify()
			}
		}
	}

	ContentKuti(appState) {
		TextFieldKuti("Prettified", { inputText }) {
			inputText = it
			lastChangedIsInput = true
		}

		RowButtonKuti(Arrangement.End) {
			AutoConvertCheckbox({ autoConvert }) { appState.setAutoConvert(it) }
			ButtonKuti("Minify", minify, autoConvert)
			ButtonKuti("Prettify", prettify, autoConvert)
		}

		TextFieldKuti("Minified", { outputText }) {
			outputText = it
			lastChangedIsInput = false
		}
	}
}