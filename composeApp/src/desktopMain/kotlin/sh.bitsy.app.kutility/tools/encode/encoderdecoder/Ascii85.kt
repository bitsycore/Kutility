package sh.bitsy.app.kutility.tools.encode.encoderdecoder

import java.io.ByteArrayOutputStream

object Ascii85 : EncoderDecoder {

	private const val ASCII_OFFSET = 33
	private val POW85 = intArrayOf(85 * 85 * 85 * 85, 85 * 85 * 85, 85 * 85, 85, 1)

	override fun encode(input: ByteArray): String {
		val output = StringBuilder()
		var i = 0

		while (i < input.size) {
			val chunk = ByteArray(4)
			val len = minOf(4, input.size - i)
			for (j in 0 until len) {
				chunk[j] = input[i + j]
			}

			val value = chunk.foldIndexed(0) { idx, acc, byte ->
				acc or ((byte.toInt() and 0xFF) shl (24 - idx * 8))
			}

			if (len == 4 && value == 0) {
				output.append('z')
			} else {
				val encoded = CharArray(5)
				var temp = value
				for (j in 4 downTo 0) {
					encoded[j] = (temp % 85 + ASCII_OFFSET).toChar()
					temp /= 85
				}
				for (j in 0 until len + 1) {
					output.append(encoded[j])
				}
			}

			i += 4
		}

		return output.toString()
	}

	override fun decode(input: String): ByteArray {
		val output = ByteArrayOutputStream()
		var i = 0

		while (i < input.length) {
			if (input[i] == 'z') {
				output.write(byteArrayOf(0, 0, 0, 0))
				i++
				continue
			}

			val chunk = CharArray(5)
			var len = 0

			while (len < 5 && i + len < input.length && input[i + len] != 'z') {
				chunk[len] = input[i + len]
				len++
			}

			if (len < 5) {
				for (j in len until 5) {
					chunk[j] = 'u' // pad with 'u' (ASCII 117)
				}
			}

			var value = 0
			for (j in 0 until 5) {
				value += (chunk[j].code - ASCII_OFFSET) * POW85[j]
			}

			for (j in 0 until len - 1) {
				output.write((value shr (24 - j * 8)) and 0xFF)
			}

			i += len
		}

		return output.toByteArray()
	}
}
