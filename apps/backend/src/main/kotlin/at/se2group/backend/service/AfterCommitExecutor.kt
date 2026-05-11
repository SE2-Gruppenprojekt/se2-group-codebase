package at.se2group.backend.service

import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

/**
 * Executes callbacks immediately when no transaction synchronization is active,
 * or defers them until the current transaction has successfully committed.
 *
 * This is useful for side effects such as websocket broadcasts that should only
 * become visible to clients after the database changes they describe are durable.
 */
@Component
class AfterCommitExecutor {

    fun execute(action: () -> Unit) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action()
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            }
        )
    }
}
