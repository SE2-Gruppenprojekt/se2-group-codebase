package at.aau.serg.android.core.network.auth

import at.aau.serg.android.core.network.MoshiProvider
import com.squareup.moshi.JsonClass
import java.nio.charset.StandardCharsets
import java.util.Base64

object JwtSubjectDecoder {

    @JsonClass(generateAdapter = true)
    private data class JwtPayload(
        val sub: String?
    )

    private val adapter = MoshiProvider.moshi.adapter(JwtPayload::class.java)

    fun decodeSubject(accessToken: String): String {
        val tokenParts = accessToken.split(".")
        require(tokenParts.size == 3) { "JWT must contain header, payload, and signature" }

        val payloadJson = String(
            Base64.getUrlDecoder().decode(tokenParts[1]),
            StandardCharsets.UTF_8
        )
        val payload = requireNotNull(adapter.fromJson(payloadJson)) {
            "JWT payload is empty"
        }

        return payload.sub?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("JWT subject claim is missing")
    }
}
