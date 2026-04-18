package at.aau.serg.android.viewmodel

import at.aau.serg.android.core.viewmodel.BaseViewModel
import at.aau.serg.android.errors.ErrorCatalog
import at.aau.serg.android.ui.state.LoadState
import at.aau.serg.android.util.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher
    ) : DispatcherProvider {
        override val main = dispatcher
        override val io = dispatcher
    }

    private class TestViewModel(
        dispatcher: CoroutineDispatcher
    ) : BaseViewModel(TestDispatcherProvider(dispatcher)) {

        fun <T> runRequest(
            block: suspend () -> T,
            onSuccess: (T) -> Unit,
            onError: () -> Unit
        ) {
            launchRequest(block, onSuccess, onError)
        }
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun launchRequest_setsLoadingStateInitially() = runTest {
        val vm = TestViewModel(testDispatcher)

        vm.runRequest(
            block = {
                delay(1)
                "ignored"
            },
            onSuccess = {},
            onError = {}
        )

        testDispatcher.scheduler.runCurrent()

        assertEquals(LoadState.Loading, vm.loadState.value)
    }

    @Test
    fun launchRequest_emitsSuccessState() = runTest {
        val vm = TestViewModel(testDispatcher)
        var successResult: Any? = null

        vm.runRequest(
            block = { "OK" },
            onSuccess = { successResult = it },
            onError = {}
        )

        // Start the coroutine
        testDispatcher.scheduler.runCurrent()

        // Let it finish
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoadState.Success, vm.loadState.value)
        assertEquals("OK", successResult)
    }


    @Test
    fun launchRequest_usesDefaultOnSuccessCallback() = runTest {
        val vm = TestViewModel(testDispatcher)

        vm.runRequest(
            block = { "OK" },
            onSuccess = {},
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(LoadState.Success, vm.loadState.value)
    }

    @Test
    fun launchRequest_emitsErrorState_onException() = runTest {
        val vm = TestViewModel(testDispatcher)
        var errorCalled = false

        vm.runRequest(
            block = { throw RuntimeException() },
            onSuccess = {},
            onError = { errorCalled = true }
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.loadState.value as LoadState.Error
        assertEquals(ErrorCatalog.UNKNOWN, state.message)
        assertEquals(true, errorCalled)
    }

    @Test
    fun launchRequest_usesDefaultOnErrorCallback() = runTest {
        val vm = TestViewModel(testDispatcher)

        vm.runRequest(
            block = { throw RuntimeException() },
            onSuccess = {},
            onError = {}
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = vm.loadState.value as LoadState.Error
        assertEquals(ErrorCatalog.UNKNOWN, state.message)
    }
}
