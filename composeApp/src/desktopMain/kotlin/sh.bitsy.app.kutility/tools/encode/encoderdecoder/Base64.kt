@file:OptIn(ExperimentalEncodingApi::class)

package sh.bitsy.app.kutility.tools.encode.encoderdecoder

import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.encoding.Base64 as Base64KTX


object Base64 : EncoderDecoder {
	override fun encode(input: ByteArray): String = Base64KTX.encode(input)
	override fun decode(input: String): ByteArray = Base64KTX.decode(input)
}