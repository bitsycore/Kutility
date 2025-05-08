package sh.bitsy.app.kutility.tools.encoding

object Hex : EncoderDecoder {

	override fun encode(input: ByteArray): String {
		return input.joinToString("") { String.format("%02X", it) }
	}

	override fun decode(input: String): ByteArray {
		// Remove 0x prefix if present
		// Remove any non-hexadecimal characters
		val filteredInput = input
			.replace("0x", "", ignoreCase = true)
			.filter { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }

		if (filteredInput.length % 2 != 0) throw IllegalArgumentException("Hex string length must be even.")

		val result = ByteArray(filteredInput.length / 2)
		for (i in 0 until filteredInput.length step 2) {
			result[i / 2] = ((Character.digit(filteredInput[i], 16) shl 4) + Character.digit(filteredInput[i + 1], 16)).toByte()
		}

		return result
	}
}
