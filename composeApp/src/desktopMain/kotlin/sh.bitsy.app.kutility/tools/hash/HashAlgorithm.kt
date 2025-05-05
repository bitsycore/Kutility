package sh.bitsy.app.kutility.tools.hash

import java.security.Security

@JvmInline
value class HashAlgorithm(val name: String) {
    companion object {
        val availableAlgorithm: Set<HashAlgorithm> by lazy {
            Security.getAlgorithms("MessageDigest")
                .map { HashAlgorithm(it.uppercase()) }
                .sortedBy { it.name }
                .toSet()
        }
    }
}