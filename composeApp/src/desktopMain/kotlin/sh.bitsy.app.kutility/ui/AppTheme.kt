package sh.bitsy.app.kutility.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor

enum class AppThemeType {
	SYSTEM,
	LIGHT,
	DARK
}

val LocalAppTheme = staticCompositionLocalOf { AppTheme.LIGHT }

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
	val type: AppThemeType,
	val textColor: Color,
	val bg1Color: Color,
	val bg2Color: Color,
	val borderColor: Color,
	val disabledBgColor: Color,
	val disabledTextColor: Color,
	val grayColor: Color,
	val bgStripWidth: Dp = 3.dp
) {

	private var _invertTheme: AppTheme? = null
	val invertTheme: AppTheme get() = _invertTheme!!

	companion object {
		val LIGHT = AppTheme(
			type = AppThemeType.LIGHT,
			textColor = Color(0xFF000000),
			bg1Color = Color(0xFFFFFFFF),
			bg2Color = Color(0xFFF0F0F0),
			borderColor = Color(0xFFBDBDBD),
			grayColor = Color(0xffcccccc),
			disabledBgColor = Color(0xffaaaaaa),
			disabledTextColor = Color(0xff858585),
		)
		val DARK = AppTheme(
			type = AppThemeType.DARK,
			textColor = Color(0xFFFFFFFF),
			bg1Color = Color(0xFF121212),
			bg2Color = Color(0xFF1E1E1E),
			borderColor = Color(0xff404040),
			grayColor = Color(0xff333333),
			disabledBgColor = Color(0xff373434),
			disabledTextColor = Color(0xff595757),
		)

		init {
			LIGHT._invertTheme = DARK
			DARK._invertTheme = LIGHT
		}
	}
}