package sh.bitsy.app.kutility.tools.encode

import kotlinx.coroutines.flow.MutableStateFlow

data class EncodingScreenState(
	val inputText: MutableStateFlow<String> = MutableStateFlow(""),
	val outputText: MutableStateFlow<String> = MutableStateFlow(""),
	val selectedAlgorithm: MutableStateFlow<EncodingFormat> = MutableStateFlow(EncodingFormat.BASE64),
	val selectedTextFormat: MutableStateFlow<TextFormat> = MutableStateFlow(TextFormat.default),
)