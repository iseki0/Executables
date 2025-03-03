package space.iseki.executables.pe.vi

import space.iseki.executables.share.u2l
import space.iseki.executables.pe.getWString
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal data class StructureHeader(
    val wLength: UShort,
    val wValueLength: UShort,
    val wType: UShort,
    val szKey: String,
    val offset: Int,
    val padding: Int,
    val length: Int,
)

internal fun parseStructureHeader(bytes: ByteArray, off: Int): StructureHeader {
    var pos = off
    val wLength = bytes.u2l(pos)
    pos += 2
    val wValueLength = bytes.u2l(pos)
    pos += 2
    val wType = bytes.u2l(pos)
    pos += 2
    val szKey = bytes.getWString(pos)
    pos += szKey.length * 2 + 2
    val padding = paddingAlignTo32Bit(pos)
    return StructureHeader(
        wLength = wLength,
        wValueLength = wValueLength,
        wType = wType,
        szKey = szKey,
        offset = off,
        padding = padding,
        length = pos - off + padding,
    )
}

internal fun paddingAlignTo32Bit(off: Int): Int {
    return if (off % 4 == 0) 0 else 4 - off % 4
}

internal class StructureHeaderCheckScope(val header: StructureHeader) {
    var errors: ArrayList<StructureCheckFailedException.Entry>? = null
        private set

    fun expectWValueLength(v: UShort) {
        if (v != header.wValueLength) failed("wValueLength", header.wValueLength, v)
    }

    fun expectWType(v: UShort) {
        if (v != header.wType) failed("wType", header.wType, v)
    }

    fun expectSzKey(v: String) {
        if (v != header.szKey) failed("szKey", header.szKey, v)
    }

    fun <T> failed(field: String, expected: T, actual: T) {
        if (errors == null) errors = ArrayList()
        errors!!.add(StructureCheckFailedException.CheckEntry(field, expected, actual))
    }

    fun failed(field: String, message: String) {
        if (errors == null) errors = ArrayList()
        errors!!.add(StructureCheckFailedException.FailEntry(field, message))
    }

    fun doThrow(header: StructureHeader) {
        if (errors.isNullOrEmpty()) return
        throw StructureCheckFailedException(header, errors!!)
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun StructureHeader.check(block: StructureHeaderCheckScope.() -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    val scope = StructureHeaderCheckScope(this)
    scope.block()
    scope.doThrow(this)
}

internal class StructureCheckFailedException(
    private val header: StructureHeader,
    private val entries: List<Entry>,
) : RuntimeException() {
    interface Entry {
        val key: String
    }

    data class CheckEntry<T>(override val key: String, val expected: T, val actual: T) : Entry
    data class FailEntry(override val key: String, val message: String) : Entry

    override val message by lazy(LazyThreadSafetyMode.PUBLICATION) {
        entries.joinToString(prefix = "$header\n", separator = "\n") { "  - $it" }
    }
}
