package at.aau.serg.android.core.network.mapper

import at.aau.serg.android.core.errors.AppError
import io.mockk.every
import io.mockk.mockk
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class NetworkErrorMapperTest {

    private fun httpException(code: Int, body: String? = null): HttpException {
        // Creates a real response object. If body is null, we provide a valid but empty-logic body.
        val responseBody = (body ?: "").toResponseBody("application/json".toMediaType())
        val response = Response.error<Any>(code, responseBody)
        return HttpException(response)
    }

    @Test
    fun map_returnsNetworkError_forIOException() {
        assertEquals(AppError.Rest.Network, NetworkErrorMapper.map(IOException()))
    }

    @Test
    fun map_returnsUnknown_forGeneralException() {
        assertEquals(AppError.Unknown, NetworkErrorMapper.map(RuntimeException()))
    }

    @Test
    fun map_returnsApiError_forValidJson() {
        val json = """{"errorMessage": "Valid Error"}"""
        val ex = httpException(400, json)
        assertEquals(AppError.Rest.Api("Valid Error"), NetworkErrorMapper.map(ex))
    }

    @Test
    fun map_returnsBadRequest_forBlankMessage() {
        // Hits the .isNotBlank() == false branch
        val json = """{"errorMessage": "  "}"""
        val ex = httpException(400, json)
        assertEquals(AppError.Rest.BadRequest, NetworkErrorMapper.map(ex))
    }

    @Test
    fun map_handles_null_response_object() {
        // This targets the very first ?. in the chain: e.response()?.
        val ex = mockk<HttpException>()
        every { ex.response() } returns null
        every { ex.code() } returns 404

        assertEquals(AppError.Rest.NotFound, NetworkErrorMapper.map(ex))
    }

    @Test
    fun map_handles_null_errorBody() {
        // This targets the second ?. in the chain: .errorBody()?.
        val response = mockk<Response<*>>()
        val ex = mockk<HttpException>()
        every { ex.response() } returns response
        every { ex.code() } returns 403
        every { response.errorBody() } returns null

        assertEquals(AppError.Rest.Forbidden, NetworkErrorMapper.map(ex))
    }

    @Test
    fun map_handles_malformed_json_parsing_to_null() {
        // Targets the body?.let { adapter.fromJson(it) } where it might return null
        // or the errorMessage field itself is missing.
        val ex = httpException(409, """{"errorCode": "NO_MESSAGE_FIELD"}""")
        assertEquals(AppError.Rest.Conflict, NetworkErrorMapper.map(ex))
    }

    @Test
    fun map_returnsServer_for_boundary_500() {
        val ex = httpException(500, "{}")
        val result = NetworkErrorMapper.map(ex)
        assertEquals(AppError.Rest.Server, result)
    }

    @Test
    fun map_returnsServer_for_boundary_599() {
        val ex = httpException(599, "{}")
        val result = NetworkErrorMapper.map(ex)
        assertEquals(AppError.Rest.Server, result)
    }

    @Test
    fun map_returnsUnknown_for_code_outside_range() {
        var ex = httpException(418, "{}")
        var result = NetworkErrorMapper.map(ex)
        assertEquals(AppError.Unknown, result)

        ex = httpException(600, "{}")
        result = NetworkErrorMapper.map(ex)
        assertEquals(AppError.Unknown, result)
    }
}
