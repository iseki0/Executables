package space.iseki.executables.pe

import kotlin.jvm.JvmInline

class DataDirectories {
    @JvmInline
    value class Key internal constructor(val index: Int) {
        init {
            check(index in 0..15)
        }

        override fun toString(): String {
            return when (index) {
                Constants.EXPORT_TABLE_INDEX -> "ExportTable"
                Constants.IMPORT_TABLE_INDEX -> "ImportTable"
                Constants.RESOURCE_TABLE_INDEX -> "ResourceTable"
                Constants.EXCEPTION_TABLE_INDEX -> "ExceptionTable"
                Constants.CERTIFICATE_TABLE_INDEX -> "CertificateTable"
                Constants.BASE_RELOCATION_TABLE_INDEX -> "BaseRelocationTable"
                Constants.DEBUG_INDEX -> "Debug"
                Constants.ARCHITECTURE_INDEX -> "Architecture"
                Constants.GLOBAL_PTR_INDEX -> "GlobalPtr"
                Constants.TLS_TABLE_INDEX -> "TLSTable"
                Constants.LOAD_CONFIG_TABLE_INDEX -> "LoadConfigTable"
                Constants.BOUND_IMPORT_INDEX -> "BoundImport"
                Constants.IAT_INDEX -> "IAT"
                Constants.DELAY_IMPORT_DESCRIPTOR_INDEX -> "DelayImportDescriptor"
                Constants.CLR_RUNTIME_HEADER_INDEX -> "CLRRuntimeHeader"
                else -> error("unreachable")
            }
        }
    }

    object Constants {
        const val EXPORT_TABLE_INDEX = 0
        const val IMPORT_TABLE_INDEX = 1
        const val RESOURCE_TABLE_INDEX = 2
        const val EXCEPTION_TABLE_INDEX = 3
        const val CERTIFICATE_TABLE_INDEX = 4
        const val BASE_RELOCATION_TABLE_INDEX = 5
        const val DEBUG_INDEX = 6
        const val ARCHITECTURE_INDEX = 7
        const val GLOBAL_PTR_INDEX = 8
        const val TLS_TABLE_INDEX = 9
        const val LOAD_CONFIG_TABLE_INDEX = 10
        const val BOUND_IMPORT_INDEX = 11
        const val IAT_INDEX = 12
        const val DELAY_IMPORT_DESCRIPTOR_INDEX = 13
        const val CLR_RUNTIME_HEADER_INDEX = 14
    }

    companion object {
        val ExportTable = Key(Constants.EXPORT_TABLE_INDEX)
        val ImportTable = Key(Constants.IMPORT_TABLE_INDEX)
        val ResourceTable = Key(Constants.RESOURCE_TABLE_INDEX)
        val ExceptionTable = Key(Constants.EXCEPTION_TABLE_INDEX)
        val CertificateTable = Key(Constants.CERTIFICATE_TABLE_INDEX)
        val BaseRelocationTable = Key(Constants.BASE_RELOCATION_TABLE_INDEX)
        val Debug = Key(Constants.DEBUG_INDEX)
        val Architecture = Key(Constants.ARCHITECTURE_INDEX)
        val GlobalPtr = Key(Constants.GLOBAL_PTR_INDEX)
        val TLSTable = Key(Constants.TLS_TABLE_INDEX)
        val LoadConfigTable = Key(Constants.LOAD_CONFIG_TABLE_INDEX)
        val BoundImport = Key(Constants.BOUND_IMPORT_INDEX)
        val IAT = Key(Constants.IAT_INDEX)
        val DelayImportDescriptor = Key(Constants.DELAY_IMPORT_DESCRIPTOR_INDEX)
        val CLRRuntimeHeader = Key(Constants.CLR_RUNTIME_HEADER_INDEX)
    }
}