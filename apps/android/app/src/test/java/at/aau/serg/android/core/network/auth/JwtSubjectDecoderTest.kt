package at.aau.serg.android.core.network.auth

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Base64

class JwtSubjectDecoderTest {

    @Test
    fun decodeSubject_returnsSubject_forValidJwt() {
        val token = jwt("""{"sub":"user-123"}""")

        assertEquals("user-123", JwtSubjectDecoder.decodeSubject(token))
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeSubject_throws_whenJwtPartCountIsInvalid() {
        JwtSubjectDecoder.decodeSubject("not.a.jwt.extra")
    }

    @Test(expected = IllegalStateException::class)
    fun decodeSubject_throws_whenSubjectIsMissing() {
        JwtSubjectDecoder.decodeSubject(jwt("""{"sub":null}"""))
    }

    @Test(expected = IllegalStateException::class)
    fun decodeSubject_throws_whenSubjectIsBlank() {
        JwtSubjectDecoder.decodeSubject(jwt("""{"sub":"   "}"""))
    }

    @Test(expected = IllegalArgumentException::class)
    fun decodeSubject_throws_whenPayloadBodyIsNull() {
        JwtSubjectDecoder.decodeSubject(jwt("null"))
    }

    private fun jwt(payloadJson: String): String {
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val header = encoder.encodeToString("""{"alg":"none"}""".toByteArray())
        val payload = encoder.encodeToString(payloadJson.toByteArray())
        return "$header.$payload.signature"
    }
}
