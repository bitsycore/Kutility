package sh.bitsy.app.kutility.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor

val LocalAppTheme = staticCompositionLocalOf { AppTheme.lightTheme }

@Composable
fun ProvideTheme(
	theme: AppTheme,
	content: @Composable () -> Unit
) {
	CompositionLocalProvider(LocalAppTheme provides theme) {
		CompositionLocalProvider(LocalContentColor provides theme.textColor) {
			content()
		}
	}
}

@ConsistentCopyVisibility
data class AppTheme private constructor(
	val textColor: Color,
	val bg1Color: Color,
	val bg2Color: Color,
	val borderColor: Color,
	val grayColor: Color = Color(0xff808080),
	val bgStripWidth: Dp = 3.dp
) {
	private var _invertTheme: AppTheme? = null
	val invertTheme: AppTheme get() = _invertTheme!!

	companion object {
		val lightTheme = AppTheme(
			textColor = Color(0xFF000000),
			bg1Color = Color(0xFFFFFFFF),
			bg2Color = Color(0xFFF0F0F0),
			borderColor = Color(0xFFBDBDBD),
			grayColor = Color(0xffcccccc),
		)
		val darkTheme = AppTheme(
			textColor = Color(0xFFFFFFFF),
			bg1Color = Color(0xFF121212),
			bg2Color = Color(0xFF1E1E1E),
			borderColor = Color(0xff404040),
			grayColor = Color(0xff333333),
		)

		init {
			lightTheme._invertTheme = darkTheme
			darkTheme._invertTheme = lightTheme
		}
	}
}