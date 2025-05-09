package sh.bitsy.app.kutility.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.extensions.toHex
import sh.bitsy.app.kutility.tools.HashAlgorithm
import sh.bitsy.app.kutility.ui.AutoConvertCheckbox
import sh.bitsy.app.kutility.ui.ButtonKuti
import sh.bitsy.app.kutility.ui.ContentKuti
import sh.bitsy.app.kutility.ui.ExpandedMenuKuti
import sh.bitsy.app.kutility.ui.MenuItemKuti
import sh.bitsy.app.kutility.ui.RowButtonKuti
import sh.bitsy.app.kutility.ui.TextFieldKuti
import java.security.MessageDigest.getInstance
import java.security.NoSuchAlgorithmException

@Composable
fun HashScreen(appState: AppState) {

	var inputText by remember { mutableStateOf("") }
	var outputText by remember { mutableStateOf("") }
	var selectedAlgorithm by remember { mutableStateOf(HashAlgorithm.Companion.defaultAlgorithm) }

	val autoConvert by appState.autoConvert.collectAsState()

	val performHash = {
		outputText = try {
			val digest = getInstance(selectedAlgorithm.name)
			val hashBytes = digest.digest(inputText.toByteArray(Charsets.UTF_8))
			hashBytes.toHex()
		} catch (_: NoSuchAlgorithmException) {
			"Error: Algorithm '${selectedAlgorithm.name}' not supported by this JVM."
		} catch (e: Exception) {
			"Error: ${e.localizedMessage}"
		}
	}

	LaunchedEffect(inputText, selectedAlgorithm, autoConvert) {
		if (autoConvert) {
			performHash()
		}
	}

	ContentKuti() {

		TextFieldKuti(
			placeHolder = "Input Text",
			textValue = { inputText },
			onTextChange = { inputText = it }
		)

		RowButtonKuti(Arrangement.SpaceBetween) {
			ExpandedMenuKuti("Algorithm: ${selectedAlgorithm.name}") {
				HashAlgorithm.Companion.availableAlgorithm.forEachIndexed { index, option ->
					MenuItemKuti(option.name) {
						selectedAlgorithm = option
					}
				}
			}
			RowButtonKuti(Arrangement.End) {
				AutoConvertCheckbox({ autoConvert }) { appState.setAutoConvert(it) }
				ButtonKuti("Hash", performHash, autoConvert)
			}
		}

		TextFieldKuti(
			placeHolder = "Output Hash (Hex)",
			textValue = { outputText },
			oneLine = true,
		)
	}
}