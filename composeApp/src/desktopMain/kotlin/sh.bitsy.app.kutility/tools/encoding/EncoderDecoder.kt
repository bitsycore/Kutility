package sh.bitsy.app.kutility.tools.encoding

interface EncoderDecoder {
	fun encode(input: ByteArray): String
	fun decode(input: String): ByteArray
}