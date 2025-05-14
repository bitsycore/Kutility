package sh.bitsy.app.kutility.ui

import androidx.compose.foundation.DarkDefaultContextMenuRepresentation
import androidx.compose.foundation.LightDefaultContextMenuRepresentation
import androidx.compose.foundation.LocalContextMenuRepresentation
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composeunstyled.LocalContentColor
import sh.bitsy.app.kutility.ui.AppThemeType.DARK
import sh.bitsy.app.kutility.ui.AppThemeType.LIGHT
import sh.bitsy.app.kutility.ui.AppThemeType.SYSTEM

enum class AppThemeType {
	SYSTEM,
	LIGHT,
	DARK;
}

val AppThemeType.theme: AppTheme
	@Composable
	get() = when (this) {
		SYSTEM -> if (isSystemInDarkTheme()) AppTheme.DARK else AppTheme.LIGHT
		LIGHT -> AppTheme.LIGHT
		DARK -> AppTheme.DARK
	}

val LocalAppTheme = staticCompositionLocalOf { AppTheme.LIGHT }

@Composable
inline fun ProvideAppTheme(
	themeType: AppThemeType,
	crossinline content: @Composable ((AppTheme) -> Unit)
) {
	val isDarkTheme = when(themeType) {
		SYSTEM -> isSystemInDarkTheme()
		LIGHT -> false
		DARK -> true
	}

	val contextMenuRepresentation = when (isDarkTheme) {
		false -> LightDefaultContextMenuRepresentation
		true -> DarkDefaultContextMenuRepresentation
	}

	CompositionLocalProvider(
		LocalAppTheme provides themeType.theme,
		LocalContentColor provides themeType.theme.textColor,
		LocalContextMenuRepresentation provides contextMenuRepresentation
	) {
		content(themeType.theme)
	}
}

@ConsistentCopyVisibility
data class AppTheme private constructor(
	val type: AppThemeType,
	val textColor: Color,
	val bg1Color: Color,
	val bg2Color: Color,
	val borderColor: Color,
	val selectedTextColor: Color,
	val selectedBgColor: Color,
	val selectedBorderColor: Color,
	val disabledBgColor: Color,
	val disabledTextColor: Color,
	val grayColor: Color,
	val bgStripWidth: Dp = 3.dp,
	val buttonShape: Shape = RoundedCornerShape(8.dp)//(8.dp),
) {
	companion object {
		val LIGHT = AppTheme(
			type = AppThemeType.LIGHT,
			textColor = Color(0xff090909),
			bg1Color = Color(0xfffbfafa),
			bg2Color = Color(0xFFF0F0F0),
			borderColor = Color(0xFFBDBDBD),
			grayColor = Color(0xffcccccc),
			disabledBgColor = Color(0xffaaaaaa),
			disabledTextColor = Color(0xff858585),
			selectedTextColor = Color(0xfffbfafa),
			selectedBgColor = Color(0xff5a3d95),
			selectedBorderColor = Color(0xffa38fcc),
		)
		val DARK = AppTheme(
			type = AppThemeType.DARK,
			textColor = Color(0xfffbfafa),
			bg1Color = Color(0xFF121212),
			bg2Color = Color(0xFF1E1E1E),
			borderColor = Color(0xff404040),
			grayColor = Color(0xff333333),
			disabledBgColor = Color(0xff373434),
			disabledTextColor = Color(0xff595757),
			selectedTextColor = Color(0xFF121212),
			selectedBgColor = Color(0xffa38fcc),
			selectedBorderColor = Color(0xff5a3d95),
		)
	}
}