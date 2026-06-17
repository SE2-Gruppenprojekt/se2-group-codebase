package at.aau.serg.android.core.network.auth

import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class UnauthorizedSessionInterceptorTest {

    @Test
    fun intercept_callsHandler_whenResponseIs401() {
        var unauthorizedCalls = 0
        val interceptor = UnauthorizedSessionInterceptor(
            object : UnauthorizedSessionHandler {
                override fun onUnauthorized() {
                    unauthorizedCalls++
                }
            }
        )
        val chain = StatusCodeChain(401)

        interceptor.intercept(chain)

        assertEquals(1, unauthorizedCalls)
    }

    @Test
    fun intercept_doesNotCallHandler_whenResponseIsNot401() {
        var unauthorizedCalls = 0
        val interceptor = UnauthorizedSessionInterceptor(
            object : UnauthorizedSessionHandler {
                override fun onUnauthorized() {
                    unauthorizedCalls++
                }
            }
        )
        val chain = StatusCodeChain(200)

        interceptor.intercept(chain)

        assertEquals(0, unauthorizedCalls)
    }

    private class StatusCodeChain(
        private val responseCode: Int
    ) : Interceptor.Chain {
        private val request = Request.Builder().url("https://example.com").build()

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(responseCode)
                .message("response")
                .body("body".toResponseBody())
                .build()
        }

        override fun connection() = null
        override fun call() = throw UnsupportedOperationException()
        override fun connectTimeoutMillis(): Int = 0
        override fun withConnectTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun readTimeoutMillis(): Int = 0
        override fun withReadTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
        override fun writeTimeoutMillis(): Int = 0
        override fun withWriteTimeout(timeout: Int, unit: java.util.concurrent.TimeUnit) = this
    }
}
