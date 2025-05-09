package sh.bitsy.app.kutility.tools

import sh.bitsy.app.kutility.tools.encoderdecoder.Ascii85
import sh.bitsy.app.kutility.tools.encoderdecoder.Base45
import sh.bitsy.app.kutility.tools.encoderdecoder.Base64
import sh.bitsy.app.kutility.tools.encoderdecoder.EncoderDecoder
import sh.bitsy.app.kutility.tools.encoderdecoder.Hex

enum class EncodeFormat(val encoder: EncoderDecoder) {
	HEX(Hex),
	BASE45(Base45),
	BASE64(Base64),
	ASCII85(Ascii85),
}