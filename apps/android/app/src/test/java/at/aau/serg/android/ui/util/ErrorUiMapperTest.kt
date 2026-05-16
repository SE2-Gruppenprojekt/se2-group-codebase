package at.aau.serg.android.ui.util

import at.aau.serg.android.core.errors.AppError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorUiMapperTest {

    @Test
    fun maps_all_static_errors() {
        AppError.allStaticErrors().forEach { error ->
            val result = ErrorUiMapper.toMessage(error)
            assert(result.isNotBlank())
        }
    }

    @Test
    fun maps_api_error_with_message() {
        val error = AppError.Rest.Api("custom failure")

        assertEquals(
            "custom failure",
            ErrorUiMapper.toMessage(error)
        )
    }

    @Test
    fun maps_state_error() {
        var error = AppError.State("custom failure")

        assertEquals(
            "custom failure",
            ErrorUiMapper.toMessage(error)
        )
    }

    @Test
    fun maps_unknown_error_with_message() {
        val error = AppError.Unknown("custom failure")

        assertEquals(
            "custom failure",
            ErrorUiMapper.toMessage(error)
        )
    }

    @Test
    fun maps_unknown_network_error_with_message() {
        val error = AppError.UnknownNetwork("custom failure")

        assertEquals(
            "custom failure",
            ErrorUiMapper.toMessage(error)
        )
    }

    @Test
    fun maps_websocket_connection_failed_error_with_message() {
        val error = AppError.WebSocket.ConnectionFailed("custom failure")

        assertEquals(
            "custom failure",
            ErrorUiMapper.toMessage(error)
        )
    }


    @Test
    fun maps_invalidStateException_to_appError_state() {
        var result = ErrorUiMapper.map(IllegalStateException("test"))
        assertTrue(result is AppError.State)
        assertEquals("test", (result as AppError.State).message)


        result = ErrorUiMapper.map(IllegalStateException())
        assertTrue(result is AppError.State)
        assertEquals("Unexpected State Error", (result as AppError.State).message)
    }

    @Test
    fun maps_unmapped_to_apperror_unknown() {
        var result = ErrorUiMapper.map(RuntimeException("test"))
        assertTrue(result is AppError.Unknown)
        assertEquals("test", (result as AppError.Unknown).message)

        result = ErrorUiMapper.map(RuntimeException())
        assertTrue(result is AppError.Unknown)
        assertEquals("Unexpected Error", (result as AppError.Unknown).message)
    }
}
