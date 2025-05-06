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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.Icon
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.MenuState
import com.composeunstyled.Button
import com.composeunstyled.Checkbox
import com.composeunstyled.Text
import kotlinx.coroutines.flow.MutableStateFlow
import kutility.composeapp.generated.resources.Res
import kutility.composeapp.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import sh.bitsy.app.kutility.AppState
import sh.bitsy.app.kutility.TextField
import sh.bitsy.app.kutility.extensions.collectAsMutableState
import sh.bitsy.app.kutility.extensions.toHex
import java.security.MessageDigest.getInstance
import java.security.NoSuchAlgorithmException

data class HashScreenState(
    val inputText: MutableStateFlow<String> = MutableStateFlow(""),
    val outputText: MutableStateFlow<String> = MutableStateFlow(""),
    val selectedAlgorithm: MutableStateFlow<HashAlgorithm> = MutableStateFlow(HashAlgorithm.defaultAlgorithm),
    val autoConvert: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val dropdownState: MenuState = MenuState(expanded = false)
)

fun DrawScope.drawDiagonalStripes() {
    val stripeWidth = 40f
    val colors = listOf(Color.Red, Color.Blue)

    // Draw the diagonal stripes
    for (i in 0..(size.width / stripeWidth).toInt()) {
        drawLine(
            color = colors[i % 2], // Alternate between the two colors
            start = Offset(x = i * stripeWidth, y = 0f),
            end = Offset(x = (i + 1) * stripeWidth, y = size.height),
            strokeWidth = stripeWidth
        )
    }
}

@Composable
fun HashScreen(appState: AppState) {

    val state = remember { HashScreenState() }

    var inputText by state.inputText.collectAsMutableState()
    var outputText by state.outputText.collectAsMutableState()
    var selectedAlgorithm by state.selectedAlgorithm.collectAsMutableState()
    var autoConvert by state.autoConvert.collectAsMutableState()

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
            borderColor = Color.Black,
            modifier = Modifier.fillMaxWidth().height(150.dp),
            singleLine = false,
            contentPadding = PaddingValues(8.dp),
            backgroundColor = Color.White,
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
                        .background(Color.White)
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = "Algorithm: ${selectedAlgorithm.name}",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }
                MenuContent(
                    modifier = Modifier.width(320.dp)
                        .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                        .background(Color.White)
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
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Checkbox(
                    checked = autoConvert,
                    onCheckedChange = { autoConvert = it },
                    shape = RoundedCornerShape(4.dp),
                    backgroundColor = Color.White,
                    borderWidth = 1.dp,
                    borderColor = Color.Black.copy(0.33f),
                    modifier = Modifier.padding(horizontal = 8.dp).size(24.dp),
                    contentDescription = "Auto Convert"
                ) {
//                    Icon(
//                        painterResource(Res.drawable.compose_multiplatform),
//                        contentDescription = null
//                    )
                    Text(
                        text = "✓",
                        color = Color.Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxSize()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .padding(2.dp)
                    )
                }

                Button(
                    onClick = performHash,
                    borderColor = Color(0xFFBDBDBD),
                    borderWidth = 1.dp,
                    backgroundColor = Color.White,
                    contentPadding = PaddingValues(8.dp),
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
            borderColor = Color.Black,
            contentPadding = PaddingValues(8.dp),
            backgroundColor = Color.White,
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            editable = true,
            singleLine = false,
            shape = RoundedCornerShape(8.dp),
        )
    }
}