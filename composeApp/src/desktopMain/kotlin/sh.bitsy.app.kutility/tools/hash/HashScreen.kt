package sh.bitsy.app.kutility.tools.hash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.extensions.toHex
import sh.bitsy.app.kutility.tools.AutoConvertCheckbox
import sh.bitsy.app.kutility.tools.ButtonKuti
import sh.bitsy.app.kutility.tools.ContentKuti
import sh.bitsy.app.kutility.tools.ExpandedMenuKuti
import sh.bitsy.app.kutility.tools.MenuItemKuti
import sh.bitsy.app.kutility.tools.RowButtonKuti
import sh.bitsy.app.kutility.tools.TextFieldKuti
import java.security.MessageDigest.getInstance
import java.security.NoSuchAlgorithmException

@Composable
fun HashScreen(appState: AppState) {

	val state = remember { HashScreenState() }

	var inputText by state.inputText.collectAsMutableState()
	var outputText by state.outputText.collectAsMutableState()
	var selectedAlgorithm by state.selectedAlgorithm.collectAsMutableState()
	val autoConvert by appState.autoConvert.collectAsState()

	val performHash = {
		state.outputText.value = try {
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

	ContentKuti(appState) {

		TextFieldKuti(
			placeHolder = "Input Text",
			textValue = { inputText },
			onTextChange = { inputText = it }
		)

		RowButtonKuti(Arrangement.SpaceBetween) {
			ExpandedMenuKuti("Algorithm: ${selectedAlgorithm.name}") {
				HashAlgorithm.availableAlgorithm.forEachIndexed { index, option ->
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