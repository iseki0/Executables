package space.iseki.executables.pe

import space.iseki.executables.common.Address32
import space.iseki.executables.common.openNativeFileDataAccessor
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CryptdlgDllTest {
    private val exports = listOf(
        PEExportSymbol("CertConfigureTrustA", 1u, Address32(0x000013f0), false, null),
        PEExportSymbol("CertConfigureTrustW", 2u, Address32(0x000013f0), false, null),
        PEExportSymbol("CertTrustCertPolicy", 3u, Address32(0x000013f0), false, null),
        PEExportSymbol("CertTrustCleanup", 4u, Address32(0x00004480), false, null),
        PEExportSymbol("CertTrustFinalPolicy", 5u, Address32(0x00003c70), false, null),
        PEExportSymbol("CertTrustInit", 6u, Address32(0x00004410), false, null),
        PEExportSymbol("DecodeAttrSequence", 7u, Address32(0x00001b00), false, null),
        PEExportSymbol("DecodeRecipientID", 8u, Address32(0x00001ff0), false, null),
        PEExportSymbol("EncodeAttrSequence", 9u, Address32(0x00001930), false, null),
        PEExportSymbol("EncodeRecipientID", 10u, Address32(0x00001ea0), false, null),
        PEExportSymbol("FormatPKIXEmailProtection", 11u, Address32(0x00001870), false, null),
        PEExportSymbol("FormatVerisignExtension", 12u, Address32(0x00001820), false, null),
        PEExportSymbol("CertModifyCertificatesToTrust", 13u, Address32(0x00004490), false, null),
        PEExportSymbol("CertSelectCertificateA", 14u, Address32(0x000013c0), false, null),
        PEExportSymbol("CertSelectCertificateW", 15u, Address32(0x000013d0), false, null),
        PEExportSymbol("CertViewPropertiesA", 16u, Address32(0x00003020), false, null),
        PEExportSymbol("CertViewPropertiesW", 17u, Address32(0x00003030), false, null),
        PEExportSymbol("DllRegisterServer", 18u, Address32(0x000021a0), false, null),
        PEExportSymbol("DllUnregisterServer", 19u, Address32(0x000023d0), false, null),
        PEExportSymbol("GetFriendlyNameOfCertA", 20u, Address32(0x00001630), false, null),
        PEExportSymbol("GetFriendlyNameOfCertW", 21u, Address32(0x00001720), false, null),
    )
    private val imports = listOf(
        PEImportSymbol("memcpy", "msvcrt.dll", null, false),
        PEImportSymbol("__C_specific_handler", "msvcrt.dll", null, false),
        PEImportSymbol("_initterm", "msvcrt.dll", null, false),
        PEImportSymbol("malloc", "msvcrt.dll", null, false),
        PEImportSymbol("free", "msvcrt.dll", null, false),
        PEImportSymbol("_amsg_exit", "msvcrt.dll", null, false),
        PEImportSymbol("_XcptFilter", "msvcrt.dll", null, false),
        PEImportSymbol("memset", "msvcrt.dll", null, false),
        PEImportSymbol("GetTickCount", "KERNEL32.dll", null, false),
        PEImportSymbol("LocalFree", "KERNEL32.dll", null, false),
        PEImportSymbol("LocalAlloc", "KERNEL32.dll", null, false),
        PEImportSymbol("LocalReAlloc", "KERNEL32.dll", null, false),
        PEImportSymbol("SetLastError", "KERNEL32.dll", null, false),
        PEImportSymbol("WideCharToMultiByte", "KERNEL32.dll", null, false),
        PEImportSymbol("GetLastError", "KERNEL32.dll", null, false),
        PEImportSymbol("InitializeCriticalSection", "KERNEL32.dll", null, false),
        PEImportSymbol("DeleteCriticalSection", "KERNEL32.dll", null, false),
        PEImportSymbol("CompareFileTime", "KERNEL32.dll", null, false),
        PEImportSymbol("EnterCriticalSection", "KERNEL32.dll", null, false),
        PEImportSymbol("TerminateProcess", "KERNEL32.dll", null, false),
        PEImportSymbol("GetCurrentProcess", "KERNEL32.dll", null, false),
        PEImportSymbol("SetUnhandledExceptionFilter", "KERNEL32.dll", null, false),
        PEImportSymbol("UnhandledExceptionFilter", "KERNEL32.dll", null, false),
        PEImportSymbol("RtlVirtualUnwind", "KERNEL32.dll", null, false),
        PEImportSymbol("RtlLookupFunctionEntry", "KERNEL32.dll", null, false),
        PEImportSymbol("RtlCaptureContext", "KERNEL32.dll", null, false),
        PEImportSymbol("DisableThreadLibraryCalls", "KERNEL32.dll", null, false),
        PEImportSymbol("GetCurrentThreadId", "KERNEL32.dll", null, false),
        PEImportSymbol("GetCurrentProcessId", "KERNEL32.dll", null, false),
        PEImportSymbol("QueryPerformanceCounter", "KERNEL32.dll", null, false),
        PEImportSymbol("Sleep", "KERNEL32.dll", null, false),
        PEImportSymbol("LeaveCriticalSection", "KERNEL32.dll", null, false),
        PEImportSymbol("GetSystemTimeAsFileTime", "KERNEL32.dll", null, false),
        PEImportSymbol("LoadStringW", "USER32.dll", null, false),
        PEImportSymbol("GetParent", "USER32.dll", null, false),
        PEImportSymbol("SendMessageW", "USER32.dll", null, false),
        PEImportSymbol("GetWindowLongPtrW", "USER32.dll", null, false),
        PEImportSymbol("SendDlgItemMessageW", "USER32.dll", null, false),
        PEImportSymbol("EnableWindow", "USER32.dll", null, false),
        PEImportSymbol("GetDlgItem", "USER32.dll", null, false),
        PEImportSymbol("ShowWindow", "USER32.dll", null, false),
        PEImportSymbol("SetDlgItemTextA", "USER32.dll", null, false),
        PEImportSymbol("SetWindowLongPtrW", "USER32.dll", null, false),
        PEImportSymbol("SetDlgItemTextW", "USER32.dll", null, false),
        PEImportSymbol("RegOpenKeyExA", "ADVAPI32.dll", null, false),
        PEImportSymbol("RegQueryValueExA", "ADVAPI32.dll", null, false),
        PEImportSymbol("RegCloseKey", "ADVAPI32.dll", null, false),
        PEImportSymbol("WinVerifyTrust", "WINTRUST.dll", null, false),
        PEImportSymbol("WTHelperGetProvCertFromChain", "WINTRUST.dll", null, false),
        PEImportSymbol("WTHelperGetProvSignerFromChain", "WINTRUST.dll", null, false),
        PEImportSymbol("WintrustRemoveActionID", "WINTRUST.dll", null, false),
        PEImportSymbol("WintrustAddActionID", "WINTRUST.dll", null, false),
        PEImportSymbol("WTHelperCertIsSelfSigned", "WINTRUST.dll", null, false),
        PEImportSymbol("CertDuplicateStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CertRemoveEnhancedKeyUsageIdentifier", "CRYPT32.dll", null, false),
        PEImportSymbol("CertVerifyTimeValidity", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptUninstallDefaultContext", "CRYPT32.dll", null, false),
        PEImportSymbol("CertGetCertificateChain", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptInstallDefaultContext", "CRYPT32.dll", null, false),
        PEImportSymbol("CertAddStoreToCollection", "CRYPT32.dll", null, false),
        PEImportSymbol("CertControlStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CertOpenStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CertFreeCertificateChain", "CRYPT32.dll", null, false),
        PEImportSymbol("CertCloseStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptFormatObject", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptUnregisterOIDFunction", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptRegisterOIDFunction", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptDecodeObjectEx", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptDecodeObject", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptEncodeObject", "CRYPT32.dll", null, false),
        PEImportSymbol("CertGetNameStringW", "CRYPT32.dll", null, false),
        PEImportSymbol("CertCompareCertificate", "CRYPT32.dll", null, false),
        PEImportSymbol("CertFindExtension", "CRYPT32.dll", null, false),
        PEImportSymbol("CertFreeCertificateContext", "CRYPT32.dll", null, false),
        PEImportSymbol("CertDuplicateCertificateContext", "CRYPT32.dll", null, false),
        PEImportSymbol("CertGetCertificateContextProperty", "CRYPT32.dll", null, false),
        PEImportSymbol("CertFindCertificateInStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CertAddCertificateContextToStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CertAddEnhancedKeyUsageIdentifier", "CRYPT32.dll", null, false),
        PEImportSymbol("CertDeleteCertificateFromStore", "CRYPT32.dll", null, false),
        PEImportSymbol("CertSetCertificateContextProperty", "CRYPT32.dll", null, false),
        PEImportSymbol("CertDuplicateCertificateChain", "CRYPT32.dll", null, false),
        PEImportSymbol("CryptUIDlgViewCertificateW", "CRYPTUI.dll", null, false),
        PEImportSymbol("CryptUIDlgViewCertificateA", "CRYPTUI.dll", null, false),
        PEImportSymbol("CryptUIDlgSelectCertificateA", "CRYPTUI.dll", null, false),
        PEImportSymbol("CryptUIDlgSelectCertificateW", "CRYPTUI.dll", null, false),
    )

    @Test
    fun test() {
        val vsvi = """
            CompanyName: Microsoft Corporation
            FileDescription: Microsoft Common Certificate Dialogs
            FileVersion: 10.0.19041.1 (WinBuild.160101.0800)
            InternalName: CRYPTDLG
            LegalCopyright: © Microsoft Corporation. All rights reserved.
            OriginalFilename: CRYPTDLG.DLL
            ProductName: Microsoft® Windows® Operating System
            ProductVersion: 10.0.19041.1
            OleSelfRegister:
        """.trimIndent()
        PEFile.open("src/fileAccessTest/resources/pe/cryptdlg.dll").use { file ->
            val vsvia =
                file.versionInfo?.stringFileInfo?.strings.orEmpty().joinToString("\n") { (k, v) -> "$k: $v".trim() }
            assertEquals(vsvi, vsvia)
            assertNotEquals(0, file.importSymbols.size)
            assertNotEquals(0, file.exportSymbols.size)
            assertContentEquals(exports, file.exportSymbols)
            assertContentEquals(imports, file.importSymbols)
        }

    }

    @Test
    fun testReadVM() {
        PEFile.open("src/fileAccessTest/resources/pe/cryptdlg.dll").use { file ->
            val relocSection = file.sections.find { it.name == ".reloc" }!!
            println(relocSection.tableItem)
            val relocSectionData = ByteArray(relocSection.size.toInt())
            openNativeFileDataAccessor("src/fileAccessTest/resources/pe/cryptdlg.dll.sections/${relocSection.name}").use {
                assertEquals(relocSection.virtualSize.toInt(), it.readAtMost(0, relocSectionData))
            }
            val actualData = ByteArray(relocSection.size.toInt())
            relocSection.readBytes(0, actualData, 0, actualData.size)
            assertEquals(relocSectionData.size, actualData.size, "Reloc section size mismatch")
            assertContentEquals(relocSectionData, actualData, "Reloc section data mismatch")
        }
    }
}
