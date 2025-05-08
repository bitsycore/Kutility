package sh.bitsy.app.kutility.tools.encoding

object Base45 {
	private val ALPHABET_ARRAY: CharArray = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:".toCharArray()
	private val DECODING_TABLE: Map<Char, Int> = ALPHABET_ARRAY.mapIndexed { index, char -> char to index }.toMap()
	private const val BASE = 45

	fun encode(input: ByteArray): String {
		val result = StringBuilder()
		var i = 0
		val len = input.size

		while (i < len) {
			if (len - i >= 2) {

				// Combine two bytes into one 16-bit value
				val b1 = input[i].toInt() and 0xFF
				val b2 = input[i + 1].toInt() and 0xFF
				val value = b1 * 256 + b2

				// Encode into three Base45 char
				// c1 + c2*45 + c3*45*45 = value
				val c3 = value / (BASE * BASE)
				val temp = value % (BASE * BASE)
				val c2 = temp / BASE
				val c1 = temp % BASE

				result.append(ALPHABET_ARRAY[c1])
				result.append(ALPHABET_ARRAY[c2])
				result.append(ALPHABET_ARRAY[c3])
				i += 2
			} else {
				// When there is only one byte left
				val value = input[i].toInt() and 0xFF

				// Encode into two Base45 char
				// c1 + c2*45 = value
				val c2 = value / BASE
				val c1 = value % BASE

				result.append(ALPHABET_ARRAY[c1])
				result.append(ALPHABET_ARRAY[c2])
				i += 1
			}
		}
		return result.toString()
	}

	fun decode(input: String): ByteArray {
		if (input.isEmpty()) {
			return ByteArray(0)
		}

		if (input.length % 3 == 1) {
			throw IllegalArgumentException("Invalid Base45 string length: ${input.length}. Length must be multiple of 3, or 2 + multiple of 3.")
		}

		val result = mutableListOf<Byte>() // instead of ByteArrayOutputStream for non-jvm compatibility
		var i = 0
		val len = input.length

		while (i < len) {
			if (len - i >= 3) {
				// Process three characters
				val c1 = input[i]
				val c2 = input[i + 1]
				val c3 = input[i + 2]

				val b1 = DECODING_TABLE[c1] ?: throw IllegalArgumentException("Invalid character in Base45 string: '$c1'")
				val b2 = DECODING_TABLE[c2] ?: throw IllegalArgumentException("Invalid character in Base45 string: '$c2'")
				val b3 = DECODING_TABLE[c3] ?: throw IllegalArgumentException("Invalid character in Base45 string: '$c3'")

				val value = b1 + b2 * BASE + b3 * (BASE * BASE)

				// Check if the value exceeds 16 bits
				if (value > 0xFFFF) {
					throw IllegalArgumentException("Invalid Base45 sequence: Decoded value $value exceeds 16-bit limit.")
				}

				result.add((value shr 8).toByte())    // Most significant byte
				result.add((value and 0xFF).toByte()) // Least significant byte
				i += 3
			} else {
				// Process two characters (must be the last group)
				val char1 = input[i]
				val char2 = input[i + 1]

				val v1 = DECODING_TABLE[char1] ?: throw IllegalArgumentException("Invalid character in Base45 string: '$char1'")
				val v2 = DECODING_TABLE[char2] ?: throw IllegalArgumentException("Invalid character in Base45 string: '$char2'")

				val value = v1 + v2 * BASE

				// Check if the value exceeds 8 bits
				if (value > 0xFF) {
					throw IllegalArgumentException("Invalid Base45 sequence: Decoded value $value exceeds 8-bit limit for a 2-char group.")
				}
				result.add(value.toByte())
				i += 2
			}
		}
		return result.toByteArray()
	}
}