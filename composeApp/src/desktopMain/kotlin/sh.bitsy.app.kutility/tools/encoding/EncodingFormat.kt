package sh.bitsy.app.kutility.tools.encoding

enum class InputType {
	ARBITRARY,
	NUMBER,
}

enum class EncodingFormat(val encoder: EncoderDecoder, val inputType: InputType) {
	HEX(Hex, InputType.ARBITRARY),
	BASE45(Base45, InputType.ARBITRARY),
	BASE64(Base64, InputType.ARBITRARY),
	ASCII85(Ascii85, InputType.ARBITRARY),
}