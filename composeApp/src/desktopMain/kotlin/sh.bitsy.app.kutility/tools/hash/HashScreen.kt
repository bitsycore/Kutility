package sh.bitsy.app.kutility.tools.hash

import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.composeunstyled.Button
import com.composeunstyled.Checkbox
import com.composeunstyled.LocalContentColor
import com.composeunstyled.Text
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.extensions.toHex
import sh.bitsy.app.kutility.ui.LocalAppTheme
import sh.bitsy.app.kutility.ui.TextField
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

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(10.dp)
	)
	{
		Text("Hashing Tool")

		TextField(
			value = inputText,
			onValueChange = { inputText = it },
			placeholder = "Input Text",
			borderColor = LocalAppTheme.current.borderColor,
			modifier = Modifier.fillMaxWidth().height(150.dp),
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
						text = "Algorithm: ${selectedAlgorithm.name}",
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
					HashAlgorithm.availableAlgorithm.forEachIndexed { index, option ->
						MenuItem(
							modifier = Modifier.clip(RoundedCornerShape(4.dp)).fillMaxWidth(),
							onClick = { selectedAlgorithm = option }
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
					onCheckedChange = { appState.setAutoConvert(it) },
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
					onClick = performHash,
					borderColor = LocalAppTheme.current.borderColor,
					borderWidth = 1.dp,
					backgroundColor = if (autoConvert) LocalAppTheme.current.disabledBgColor else LocalAppTheme.current.bg1Color,
					contentPadding = PaddingValues(8.dp),
					contentColor = if (autoConvert) LocalAppTheme.current.disabledTextColor else LocalContentColor.current,
					shape = RoundedCornerShape(6.dp),
					enabled = !autoConvert
				) {
					Text("Hash")
				}
			}
		}
		TextField(
			value = outputText,
			onValueChange = {},
			placeholder = "Output Hash (Hex)",
			borderColor = LocalAppTheme.current.borderColor,
			contentPadding = PaddingValues(8.dp),
			backgroundColor = LocalAppTheme.current.bg1Color,
			modifier = Modifier.fillMaxWidth().wrapContentHeight(),
			editable = true,
			singleLine = false,
			shape = RoundedCornerShape(8.dp),
		)
	}
}