package sh.bitsy.app.kutility.tools.hash

import com.composables.core.MenuState
import kotlinx.coroutines.flow.MutableStateFlow

data class HashScreenState(
	val inputText: MutableStateFlow<String> = MutableStateFlow(""),
	val outputText: MutableStateFlow<String> = MutableStateFlow(""),
	val selectedAlgorithm: MutableStateFlow<HashAlgorithm> = MutableStateFlow(HashAlgorithm.defaultAlgorithm),
	val dropdownState: MenuState = MenuState(expanded = false)
)