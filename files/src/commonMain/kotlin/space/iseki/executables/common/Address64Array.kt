package space.iseki.executables.common

import kotlin.jvm.JvmInline

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class Address64Array @PublishedApi internal constructor(private val storage: ULongArray) : Collection<Address64> {
    override val size: Int
        get() = storage.size

    override fun isEmpty(): Boolean = storage.isEmpty()

    override fun iterator(): Iterator<Address64> = object : Iterator<Address64> {
        private var index = 0
        override fun hasNext() = index < storage.size
        override fun next() =
            if (index < storage.size) storage[index++].toAddr() else throw NoSuchElementException(index.toString())
    }

    override fun containsAll(elements: Collection<Address64>): Boolean {
        if (elements is Address64Array) {
            return storage.containsAll(elements.storage)
        }
        return (elements as Collection<*>).all { it is Address64 && storage.contains(it.toULong()) }
    }

    override fun contains(element: Address64): Boolean = storage.contains(element.toULong())

    operator fun get(index: Int) = storage[index].toAddr()
    operator fun set(index: Int, value: Address64) {
        storage[index] = value.value
    }

}

@OptIn(ExperimentalUnsignedTypes::class)
inline fun Address64Array(size: Int, init: (Int) -> Address64): Address64Array {
    return Address64Array(ULongArray(size) { index -> init(index).toULong() })
}


