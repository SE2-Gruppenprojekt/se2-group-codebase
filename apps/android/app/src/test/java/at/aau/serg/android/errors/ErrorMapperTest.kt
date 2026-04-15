package at.aau.serg.android.errors

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ErrorMapperTest {
    private fun httpException(
        code: Int,
        body: String? = null
    ): HttpException {
        val safeBody = body ?: """{"errorCode":"","errorMessage":""}"""

        val responseBody = safeBody.toResponseBody("application/json".toMediaType())
        val response = Response.error<Any>(code, responseBody)

        return HttpException(response)
    }


    @Test
    fun map_returnsNetworkError_forIOException() {
        val result = ErrorMapper.map(IOException("network down"))

        assertEquals(ErrorCatalog.NETWORK, result)
    }

    @Test
    fun map_returnsUnknownError_forUnexpectedException() {
        val result = ErrorMapper.map(IllegalStateException("unknown problem"))

        assertEquals(ErrorCatalog.UNKNOWN, result)
    }

    @Test
    fun map_returnsApiMessage_whenHttpExceptionContainsValidJson() {
        val json = """
            {
              "errorCode": "123",
              "errorMessage": "Something bad happened"
            }
        """.trimIndent()
        val ex = httpException(400, json)
        val result = ErrorMapper.map(ex)

        assertEquals("Something bad happened", result)
    }

    @Test
    fun map_fallsBackToStatusMessage_whenApiMessageIsBlank() {
        val json = """
            {
              "errorCode": "123",
              "errorMessage": ""
            }
        """.trimIndent()
        val ex = httpException(400, json)
        val result = ErrorMapper.map(ex)

        assertEquals("Bad request", result)
    }

    @Test
    fun map_fallsBackToStatusMessage_whenJsonIsMalformed() {
        val json = """
        {
            "errorCode": "???",
            "errorMessage": "   "
        }
        """.trimIndent()
        val ex = httpException(404, json)
        val result = ErrorMapper.map(ex)

        assertEquals("Resource not found", result)
    }


    @Test
    fun map_fallsBackToStatusMessage_whenBodyIsNull() {
        val ex = httpException(403, null)
        val result = ErrorMapper.map(ex)

        assertEquals("Access denied", result)
    }

    @Test
    fun map_returnsConflict_for409() {
        val ex = httpException(409)
        val result = ErrorMapper.map(ex)

        assertEquals("Conflict", result)
    }

    @Test
    fun map_returnsServerError_for5xx() {
        val ex = httpException(500)
        val result = ErrorMapper.map(ex)

        assertEquals(ErrorCatalog.SERVER, result)
    }

    @Test
    fun map_returnsUnknown_forUnmappedStatusCode() {
        val ex = httpException(418)
        val result = ErrorMapper.map(ex)

        assertEquals(ErrorCatalog.UNKNOWN, result)
    }
}
