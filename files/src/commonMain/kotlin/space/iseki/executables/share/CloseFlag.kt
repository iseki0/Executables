package space.iseki.executables.share

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.updateAndGet
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.IOException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

@JvmInline
internal value class CloseFlag private constructor(private val s: ULong) {
    companion object {
        private val LMASK = 0x7fffffffffffffffuL
        private val HMASK = 0x8000000000000000uL
    }

    constructor() : this(0u)

    val closed: Boolean
        get() = s and HMASK != 0uL

    val shouldDoClose: Boolean
        get() = closed && s and LMASK == 0uL

    fun acquire(): CloseFlag {
        check(!closed)
        check(s < LMASK)
        return CloseFlag(s + 1u)
    }

    fun release(): CloseFlag {
        check(s and LMASK > 0uL)
        return CloseFlag(s - 1u)
    }

    fun close(): CloseFlag {
        check(!closed)
        return CloseFlag(s or HMASK)
    }
}

internal abstract class ClosableDataAccessor : DataAccessor {
    private val flag = atomic(CloseFlag())

    @OptIn(ExperimentalContracts::class)
    inline fun <R> wrapRead(block: () -> R): R {
        contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
        flag.updateAndGet {
            if (it.closed) {
                throw IOException("already closed")
            }
            it.acquire()
        }
        try {
            return block()
        } finally {
            if (flag.updateAndGet { it.release() }.shouldDoClose) doClose()
        }
    }

    override fun close() {
        flag.updateAndGet {
            if (it.closed) {
                throw IOException("already closed")
            }
            it.close()
        }.also {
            if (it.shouldDoClose) doClose()
        }
    }

    protected abstract fun doClose()
}

