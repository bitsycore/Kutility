package sh.bitsy.app.kutility.extensions

fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }