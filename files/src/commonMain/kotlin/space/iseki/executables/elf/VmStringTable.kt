package space.iseki.executables.elf

import space.iseki.executables.common.Address64
import space.iseki.executables.common.DataAccessor
import space.iseki.executables.common.readAtMost

internal class VmStringTable(val vm: DataAccessor, val off: Address64) {
    fun getStringAt(index: Int): String {
        val buf = ByteArray(256)
        val n = vm.readAtMost(off + index.toULong(), buf)
        val nullAt = buf.indexOf(0)
        if (nullAt == -1 || nullAt > n) {
            throw ElfFileException(
                "String in VMStringTable is malformed or too long",
                "str_table_vm" to off,
                "index" to index,
            )
        }
        return buf.decodeToString(0, nullAt)
    }
}
