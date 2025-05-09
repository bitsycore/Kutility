package sh.bitsy.app.kutility.tools.json

import kotlinx.coroutines.flow.MutableStateFlow

data class JsonScreenState(
	val inputText: MutableStateFlow<String> = MutableStateFlow(""),
	val outputText: MutableStateFlow<String> = MutableStateFlow(""),
)