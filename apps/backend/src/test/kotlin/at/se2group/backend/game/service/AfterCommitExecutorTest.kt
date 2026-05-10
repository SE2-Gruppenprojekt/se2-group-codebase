package at.se2group.backend.game.service

import at.se2group.backend.service.AfterCommitExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.transaction.support.TransactionSynchronizationManager

class AfterCommitExecutorTest {

    private val afterCommitExecutor = AfterCommitExecutor()

    @AfterEach
    fun tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }

    @Test
    fun `execute runs action immediately when synchronization is not active`() {
        var executions = 0

        afterCommitExecutor.execute {
            executions++
        }

        assertEquals(1, executions)
    }

    @Test
    fun `execute runs action after commit when synchronization is active`() {
        var executions = 0

        TransactionSynchronizationManager.initSynchronization()

        afterCommitExecutor.execute {
            executions++
        }

        assertEquals(0, executions)
        TransactionSynchronizationManager.getSynchronizations()
            .forEach { it.afterCommit() }

        assertEquals(1, executions)
    }
}
