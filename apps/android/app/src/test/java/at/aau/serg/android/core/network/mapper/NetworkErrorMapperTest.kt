package at.aau.serg.android.core.network.mapper


import at.aau.serg.android.core.errors.AppError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkErrorMapperTest {
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
        val result = NetworkErrorMapper.map(IOException("network down"))

        assertEquals(AppError.Network, result)
    }

    @Test
    fun map_returnsUnknownError_forUnexpectedException() {
        val result = NetworkErrorMapper.map(IllegalStateException("unknown problem"))

        assertEquals(AppError.Unknown, result)
    }

    @Test
    fun map_returnsApiError_whenHttpExceptionContainsValidJson() {
        val json = """
        {
          "errorCode": "123",
          "errorMessage": "Something bad happened"
        }
        """.trimIndent()

        val ex = httpException(400, json)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.Api("Something bad happened"), result)
    }

    @Test
    fun map_returnsBadRequest_whenApiMessageIsBlank() {
        val json = """
        {
          "errorCode": "123",
          "errorMessage": ""
        }
        """.trimIndent()

        val ex = httpException(400, json)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.BadRequest, result)
    }

    @Test
    fun map_returnsNotFound_whenJsonIsMalformed() {
        val json = """
        {
            "errorCode": "???",
            "errorMessage": "   "
        }
        """.trimIndent()

        val ex = httpException(404, json)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.NotFound, result)
    }


    @Test
    fun map_fallsBackToStatusMessage_whenBodyIsNull() {
        val ex = httpException(403, null)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.Forbidden, result)
    }

    @Test
    fun map_returnsConflict_for409() {
        val ex = httpException(409)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.Conflict, result)
    }

    @Test
    fun map_returnsServerError_for5xx() {
        val ex = httpException(500)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.Server, result)
    }

    @Test
    fun map_returnsUnknown_forUnmappedStatusCode() {
        val ex = httpException(418)
        val result = NetworkErrorMapper.map(ex)

        assertEquals(AppError.Unknown, result)
    }
}
