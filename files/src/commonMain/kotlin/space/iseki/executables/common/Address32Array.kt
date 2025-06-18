package space.iseki.executables.common

import kotlin.jvm.JvmInline

@JvmInline
@OptIn(ExperimentalUnsignedTypes::class)
value class Address32Array @PublishedApi internal constructor(private val storage: UIntArray) : Collection<Address32> {
    override val size: Int
        get() = storage.size

    override fun isEmpty(): Boolean = storage.isEmpty()

    override fun iterator(): Iterator<Address32> = object : Iterator<Address32> {
        private var index = 0
        override fun hasNext() = index < storage.size
        override fun next() =
            if (index < storage.size) storage[index++].toAddr() else throw NoSuchElementException(index.toString())
    }

    override fun containsAll(elements: Collection<Address32>): Boolean {
        if (elements is Address32Array) {
            return storage.containsAll(elements.storage)
        }
        return (elements as Collection<*>).all { it is Address32 && storage.contains(it.toUInt()) }
    }

    override fun contains(element: Address32): Boolean = storage.contains(element.toUInt())

    operator fun get(index: Int) = storage[index].toAddr()
    operator fun set(index: Int, value: Address32) {
        storage[index] = value.value
    }

}

@OptIn(ExperimentalUnsignedTypes::class)
inline fun Address32Array(size: Int, init: (Int) -> Address32): Address32Array {
    return Address32Array(UIntArray(size) { index -> init(index).toUInt() })
}


