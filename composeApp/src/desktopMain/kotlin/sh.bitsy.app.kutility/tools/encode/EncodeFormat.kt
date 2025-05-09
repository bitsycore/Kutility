package sh.bitsy.app.kutility.tools.encode

import sh.bitsy.app.kutility.tools.encode.encoderdecoder.Ascii85
import sh.bitsy.app.kutility.tools.encode.encoderdecoder.Base45
import sh.bitsy.app.kutility.tools.encode.encoderdecoder.Base64
import sh.bitsy.app.kutility.tools.encode.encoderdecoder.EncoderDecoder
import sh.bitsy.app.kutility.tools.encode.encoderdecoder.Hex

enum class EncodingFormat(val encoder: EncoderDecoder) {
	HEX(Hex),
	BASE45(Base45),
	BASE64(Base64),
	ASCII85(Ascii85),
}