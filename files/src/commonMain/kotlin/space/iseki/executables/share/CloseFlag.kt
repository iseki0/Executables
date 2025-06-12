package space.iseki.executables.share

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


internal abstract class ClosableDataAccessor : DataAccessor {
    private val closeLock = ReentrantLock()
    private var closed = false

    @OptIn(ExperimentalContracts::class)
    inline fun <R> wrapRead(block: () -> R): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        closeLock.withLock {
            if (closed) throw IOException("already closed")
            return block()
        }
    }

    override fun close() {
        closeLock.withLock {
            if (closed) throw IOException("already closed")
            closed = true
            doClose()
        }
    }

    protected abstract fun doClose()
}

