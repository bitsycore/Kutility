package sh.bitsy.app.kutility.tools.encode

import java.nio.charset.Charset

data class TextFormat(val name: String, val charset: Charset) {
	companion object {
		val available: Pair<List<TextFormat>, List<TextFormat>> by lazy {
			val sorted = Charset.availableCharsets().toSortedMap { a, b ->
				when {
					a == "UTF-8" -> -1
					b == "UTF-8" -> 1
					a.startsWith("UTF", ignoreCase = true) && !b.startsWith("UTF", ignoreCase = true) -> -1
					b.startsWith("UTF", ignoreCase = true) && !a.startsWith("UTF", ignoreCase = true) -> 1
					else -> a.compareTo(b, ignoreCase = true)
				}
			}
			val prio = sorted.filterKeys { it.startsWith("UTF", ignoreCase = true) }.map { (key, value) ->
				TextFormat(key, value)
			}
			val other = sorted.filterKeys { !it.startsWith("UTF", ignoreCase = true) }.map { (key, value) ->
				TextFormat(key, value)
			}
			Pair(prio, other)
		}

		val default: TextFormat by lazy { TextFormat("UTF-8", Charsets.UTF_8) }
	}
}