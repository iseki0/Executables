package space.iseki.executables.sbom

import kotlinx.serialization.Serializable
import space.iseki.purl.PUrl

/**
 * Represents a Go module with path, version and other information.
 *
 * @property path The module path
 * @property version The module version
 * @property sum The module checksum
 * @property replace The replacement module if this module is replaced
 */
@Serializable
data class GoModule internal constructor(
    val path: String,
    val version: String,
    val sum: String? = null,
    val replace: GoModule? = null,
) {
    val purl by lazy(LazyThreadSafetyMode.PUBLICATION) {
        PUrl.Builder().apply {
            type("golang")
            val chunks = path.split('/')
            if (chunks.size > 1) {
                namespace(chunks.subList(0, chunks.size - 1))
                name(chunks.last())
            } else {
                name(path)
            }
            version(version)
        }.build().toString()
    }
}

/**
 * Represents a Go build setting as key-value pair.
 *
 * @property key The setting key
 * @property value The setting value
 */
@Serializable
data class GoBuildSetting internal constructor(
    val key: String,
    val value: String,
)

/**
 * Represents Go build information extracted from a binary.
 *
 * @property goVersion Go toolchain version
 * @property path Main package path
 * @property main Main module
 * @property deps List of dependencies
 * @property settings Build settings
 */
@Serializable
data class GoBuildInfo internal constructor(
    val goVersion: String = "",
    val path: String = "",
    val main: GoModule? = null,
    val deps: List<GoModule> = emptyList(),
    val settings: List<GoBuildSetting> = emptyList(),
) {
    companion object {
        fun parse(data: String): GoBuildInfo {
            val lines = data.lines()
            var lineNum = 1

            var path = ""
            var main: GoModule? = null
            val deps = mutableListOf<GoModule>()
            val settings = mutableListOf<GoBuildSetting>()
            var last: GoModule? = null

            try {
                for (line in lines) {
                    when {
                        line.startsWith("path\t") -> {
                            path = line.removePrefix("path\t")
                        }

                        line.startsWith("mod\t") -> {
                            val elem = line.removePrefix("mod\t").split("\t")
                            main = parseModuleLine(elem)
                            last = main
                        }

                        line.startsWith("dep\t") -> {
                            val elem = line.removePrefix("dep\t").split("\t")
                            val dep = parseModuleLine(elem)
                            deps.add(dep)
                            last = dep
                        }

                        line.startsWith("=>\t") -> {
                            val elem = line.removePrefix("=>\t").split("\t")
                            if (elem.size != 3) {
                                error("expected 3 columns for replacement; got ${elem.size}")
                            }
                            if (last == null) {
                                error("replacement with no module on previous line")
                            }
                            val replacement = GoModule(
                                path = elem[0], version = elem[1], sum = elem[2]
                            )
                            last = last.copy(replace = replacement)
                            // 修复 deps 或 main 中的引用
                            if (main === last) main = last
                            if (deps.isNotEmpty() && deps.last() === last) {
                                deps[deps.lastIndex] = last
                            }
                        }

                        line.startsWith("build\t") -> {
                            val kv = line.removePrefix("build\t")
                            val equalIndex = kv.indexOf('=')
                            if (equalIndex == -1) error("build line missing '='")

                            val rawKey = kv.substring(0, equalIndex)
                            val rawValue = kv.substring(equalIndex + 1)

                            val key = parseMaybeQuoted(rawKey)
                            val value = parseMaybeQuoted(rawValue)

                            settings.add(GoBuildSetting(key, value))
                        }
                    }
                    lineNum++
                }
            } catch (e: Exception) {
                throw IllegalArgumentException("could not parse Go build info: line $lineNum: ${e.message}", e)
            }

            return GoBuildInfo(path = path, main = main, deps = deps, settings = settings)
        }

        private fun parseModuleLine(elem: List<String>): GoModule {
            if (elem.size != 2 && elem.size != 3) {
                error("expected 2 or 3 columns; got ${elem.size}")
            }
            val (path, version) = elem
            val sum = if (elem.size == 3) elem[2] else null
            return GoModule(path, version, sum)
        }

        private fun parseMaybeQuoted(s: String): String {
            return if (s.startsWith('"') || s.startsWith('`')) {
                s.removeSurrounding("\"").removeSurrounding("`")
            } else {
                s
            }
        }
    }
} 