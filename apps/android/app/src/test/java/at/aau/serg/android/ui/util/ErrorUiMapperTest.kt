package at.aau.serg.android.ui.util

import at.aau.serg.android.core.errors.AppError
import org.junit.Assert.assertEquals
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
        val error = AppError.Api("custom failure")

        assertEquals(
            "custom failure",
            ErrorUiMapper.toMessage(error)
        )
    }
}
