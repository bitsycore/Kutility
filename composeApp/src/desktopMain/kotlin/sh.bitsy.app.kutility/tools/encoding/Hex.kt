package sh.bitsy.app.kutility.tools.encoding

object Hex : EncoderDecoder {

	override fun encode(input: ByteArray): String {
		return input.joinToString("") { String.format("%02X", it) }
	}

	override fun decode(input: String): ByteArray {
		val length = input.length
		if (length % 2 != 0) throw IllegalArgumentException("Hex string length must be even.")

		val result = ByteArray(length / 2)
		for (i in 0 until length step 2) {
			result[i / 2] = ((Character.digit(input[i], 16) shl 4) + Character.digit(input[i + 1], 16)).toByte()
		}

		return result
	}
}
