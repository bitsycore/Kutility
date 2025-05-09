package sh.bitsy.app.kutility.tools.encode.encoderdecoder

interface EncoderDecoder {
	fun encode(input: ByteArray): String
	fun decode(input: String): ByteArray
}