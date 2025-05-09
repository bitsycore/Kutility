package sh.bitsy.app.kutility.ui

import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.MenuScope
import com.composables.core.rememberMenuState
import com.composeunstyled.Button
import com.composeunstyled.Checkbox
import com.composeunstyled.LocalContentColor
import com.composeunstyled.Text

@Composable
fun TodoToolScreen() = Box(Modifier.fillMaxSize()) {
	Text("TODO", modifier = Modifier.align(Alignment.Center), fontSize = 24.sp)
}

@Composable
fun ContentKuti(content: @Composable ColumnScope.() -> Unit) = Column(
	modifier = Modifier
		.fillMaxSize()
		.padding(8.dp),
	horizontalAlignment = Alignment.CenterHorizontally,
	verticalArrangement = Arrangement.spacedBy(10.dp),
	content = content
)

@Composable
fun AutoConvertCheckbox(autoConvert: () -> Boolean, setAutoConvert: (Boolean) -> Unit) = Row(verticalAlignment = Alignment.CenterVertically) {
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
		modifier = Modifier.padding(horizontal = 8.dp).size(24.dp).shadow(1.dp, shape = RoundedCornerShape(4.dp)),
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

@Composable
fun TextFieldKuti(placeHolder: String, textValue: () -> String, oneLine: Boolean = false, onTextChange: (String) -> Unit = {}) = TextField(
	value = textValue(),
	onValueChange = onTextChange,
	placeholder = placeHolder,
	borderColor = LocalAppTheme.current.borderColor,
	modifier = Modifier.fillMaxWidth()
		.then(if (oneLine) Modifier.wrapContentHeight() else Modifier.heightIn(150.dp))
		.shadow(1.dp, shape = RoundedCornerShape(8.dp)),
	singleLine = oneLine,
	contentPadding = PaddingValues(8.dp),
	backgroundColor = LocalAppTheme.current.bg1Color,
	shape = RoundedCornerShape(8.dp),
	textAlign = TextAlign.Start
)


@Composable
fun RowButtonKuti(arrangement: Arrangement.Horizontal = Arrangement.Center, content: @Composable (RowScope.() -> Unit)) = Row(
	modifier = Modifier.fillMaxWidth(),
	verticalAlignment = Alignment.CenterVertically,
	horizontalArrangement = arrangement,
	content = content
)

@Composable
fun ButtonKuti(buttonText: String, onClick: () -> Unit, disabled: Boolean) = Button(
	onClick = onClick,
	borderColor = LocalAppTheme.current.borderColor,
	borderWidth = 1.dp,
	backgroundColor = if (disabled) LocalAppTheme.current.disabledBgColor else LocalAppTheme.current.bg1Color,
	contentPadding = PaddingValues(8.dp),
	contentColor = if (disabled) LocalAppTheme.current.disabledTextColor else LocalContentColor.current,
	shape = RoundedCornerShape(8.dp),
	modifier = Modifier.padding(end = 8.dp).shadow(1.dp, shape = RoundedCornerShape(8.dp)),
	enabled = !disabled
) {
	Text(buttonText)
}


@Composable
fun ExpandedMenuKuti(title: String, contents: @Composable MenuScope.() -> Unit) = Menu(state = rememberMenuState()) {
	MenuButton(
		Modifier
			.shadow(1.dp, shape = RoundedCornerShape(8.dp))
			.background(LocalAppTheme.current.bg1Color, shape = RoundedCornerShape(8.dp))
			.border(1.dp, LocalAppTheme.current.borderColor, RoundedCornerShape(8.dp))
	) {
		Text(
			text = title,
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
		)
	}
	MenuContent(
		modifier = Modifier.width(320.dp)
			.border(1.dp, LocalAppTheme.current.borderColor, RoundedCornerShape(8.dp))
			.shadow(1.dp, shape = RoundedCornerShape(8.dp))
			.background(LocalAppTheme.current.bg1Color, shape = RoundedCornerShape(8.dp))
			.padding(4.dp)
			.heightIn(max = 175.dp)
			.verticalScroll(rememberScrollState()),
		exit = fadeOut(),
		contents = { contents() }
	)
}

@Composable
fun MenuScope.MenuItemKuti(
	title: String,
	onClick: () -> Unit,
) = MenuItem(
	modifier = Modifier.clip(RoundedCornerShape(4.dp)).fillMaxWidth(),
	onClick = onClick
) {
	Text(title)
}


@Composable
fun SeparatorKuti() = Box(
	modifier = Modifier.fillMaxWidth()
		.padding(12.dp)
		.height(1.dp)
		.background(LocalAppTheme.current.textColor.copy(alpha = 0.1f))
)