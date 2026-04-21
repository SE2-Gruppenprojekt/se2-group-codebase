package at.aau.serg.android.core.util

import androidx.lifecycle.ViewModel
import org.junit.Assert.*
import org.junit.Test

class GenericViewModelFactoryTest {

    class TestViewModel : ViewModel()

    @Test
    fun create_returnsViewModel_whenClassMatches() {
        val factory = GenericViewModelFactory { TestViewModel() }

        val result = factory.create(TestViewModel::class.java)

        assertTrue(result is TestViewModel)
    }

    @Test
    fun create_returnsSameInstance_fromCreator() {
        val vm = TestViewModel()

        val factory = GenericViewModelFactory { vm }

        val result = factory.create(TestViewModel::class.java)

        assertSame(vm, result)
    }

    @Test
    fun create_throwsException_whenClassDoesNotMatch() {
        class AnotherViewModel : ViewModel()

        val factory = GenericViewModelFactory { TestViewModel() }

        try {
            factory.create(AnotherViewModel::class.java)
            fail("Expected IllegalArgumentException")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("Unknown ViewModel class"))
        }
    }
}
