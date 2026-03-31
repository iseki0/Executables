package space.iseki.executables.gzipdecoder

internal object GzipTestFixtures {
    // Generated with: wsl gzip -n -c <input> | xxd -p -c 0
    val longTextGzip: ByteArray = hex(
        """
        1f8b0800000000000003b5dcc971d5001045d13d512804bda77941162480
        c1b3f1f708b653634148a400499cb5aa7a77eb7fa94ff5dfdf7fbe5c9d0f
        8fafd7df6e87b3a7d3affbe1e2f436dcbcfe78781e4e3fcf9f8697ff8fef
        be7ebc0fdf4f97c3984ef3b26efb317c3dfbf6fdfce2f2eafae6f6eec7fd
        e9e1f1e9f9e5f5e7afb7f78fe1eefafefcf3f8090d8e1a5c357852836735
        78518357357853837735f86081b8f4587b61f185d517965f587f61018615
        1896605883650dd6fdfeb106cb1a2c6bb0acc1b206cb1a2c6bb0acc18935
        38b10627f727943538b10627d6e0c41a9c5883136b70620dceacc1993538
        b30667f726c81a9c5983336b70660dceacc19935b8b00617d6e0c21a5c58
        838bfb1cc31a5c58830b6b70610d2eacc19535b8b20657d6e0ca1a5c5983
        abfb26ca1a5c59832b6b70650d6eacc18d35b8b10637d6e0c61adc58839b
        5b4cb00637d6e0c61adc59833b6b70670deeacc19d35b8b30677d6e0eeb6
        83acc19d3578b0060fd6e0c11a3c5883076bf0600d1eacc1833578b8153d
        dcd1bb25fde8b6f4a35bd38f6e4f3fba45fde836f5a35bd58f6e573fba65
        fde86a9464c6d508d10c543390cd403703e10c943390ce383b138767e2f4
        4c1c9f89f3337180264ed0c4119a3843138768e2144d1ca3897334719026
        4ed2c4519a384b138769e2344d1ca789f33471a0264ed4c4919a38531387
        6ae2544d1cab89733571b0264ed6c4d19a385b13876be2744d1caf89f335
        71c0264ed8c4119b386313876ce2944d1cb389733671d0264edac4519b38
        6b13876de2b44d1cb789f33671e0264edcc4919b387313876ee2d44d1cbb
        89733771f0264edec4d19b387b13876fe2f44d1cbf89f3377100274ee0c4
        119c3883138770e2144e1cc38973387110274ee2c4519c388b536771ea2c
        4e9dc5a9b3387516a7cee2d4599c3a8b536771ea2c4e9dc5a9b3387516a7
        cee2d4599c3a8b536771ea2c4e9dc529bc63030fd9c84b36ae4678cb061e
        b381d76ce0391b78cf061eb47116a7cee2d4599c3a8b536771ea2c4e9dc5
        a9b3387516a7cee2d4599c3a8b536771ea2c4e9dc5a9b3387516a7cee2d4
        599c3a8b536771ea2c4e9dc5a9b3387516a7cee274593efd03c89bbc0595
        5a0000
        """
    )

    val longBinaryGzip: ByteArray = hex(
        """
        1f8b0800000000000003e33608ad9a7fe4259f7144eda2e36f04cda21b96
        9e7a2f6219d7bce2ec27719bc4b6d517be4ad9a774aebbfc43d629bd67e3
        b5df0aae59fd5b6efe53f6c89db4fd0ea39a77c1d45df75934fd8a67ec7d
        c4ae135836fbc0532efd90ca79875ff01a85d72c3cf65ac034aa7ec9c977
        c216b14dcbcf7c14b34e685d75fe8ba45d72c7da4bdf651cd3ba375cfd25
        ef92d9b7f9c65f25f79c89db6e33a87ae54fd9798f59c3b768fa9e876cda
        01a5b3f63fe1d40bae987be8398f6158f582a3aff84d22eb169f782b641e
        d3b8ecf40751abf89695e73e4bd826b5afb9f84dda21b56bfd959f72ce19
        bd9baeff5174cb9eb0f5d67f15cfbcc93bee32a9fb144edbfd8055cbbf64
        e6bec71cba41e5730e3ee31ef5ffa8ff47fd3feaff51ff8ffa7fd4ffa3fe
        1ff5ffa8ff47fd3feaff51ff8ffa7fd4ffa3fe1ff5ffa8ff47fd3feaff51
        ff8ffa7fd4ffa3fe1ff5ffa8ff47fd3feaff51ff8ffa7f18f81f00c2100b
        aa00200000
        """
    )

    fun longTextExpected(): ByteArray {
        val textBytes = buildString {
            for (i in 0 until 256) {
                append("The quick brown fox jumps over the lazy dog 0123456789 abcdefghijklmnopqrstuvwxyz")
                append(" line=")
                append(i)
                append('\n')
            }
        }.encodeToByteArray()
        return byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + textBytes
    }

    fun longBinaryExpected(): ByteArray = ByteArray(8192) { index ->
        ((index * 37 + 11) % 256).toByte()
    }

    fun hex(value: String): ByteArray {
        val normalized = value.filterNot(Char::isWhitespace)
        require(normalized.length % 2 == 0) { "hex string must have an even length" }
        return ByteArray(normalized.length / 2) { index ->
            normalized.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
