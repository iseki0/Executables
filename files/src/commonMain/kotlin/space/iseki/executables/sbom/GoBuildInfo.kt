package space.iseki.executables.sbom

/**
 * Represents a Go module with path, version and other information.
 *
 * @property path The module path
 * @property version The module version
 * @property sum The module checksum
 * @property replace The replacement module if this module is replaced
 */
data class GoModule(
    val path: String,
    val version: String,
    val sum: String? = null,
    val replace: GoModule? = null,
)

/**
 * Represents a Go build setting as key-value pair.
 *
 * @property key The setting key
 * @property value The setting value
 */
data class GoBuildSetting(
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
data class GoBuildInfo(
    val goVersion: String = "",
    val path: String = "",
    val main: GoModule? = null,
    val deps: List<GoModule> = emptyList(),
    val settings: List<GoBuildSetting> = emptyList(),
) {
    companion object {
        // Constants for line prefixes
        private const val PREFIX_MODULE = "path"
        private const val PREFIX_MOD = "mod"
        private const val PREFIX_DEP = "dep"
        private const val PREFIX_SUM = "sum"
        private const val PREFIX_REPLACE = "=>"
        private const val PREFIX_BUILD = "build"

        /**
         * Parses build information from its string representation.
         *
         * @param content The string content containing build info
         * @return Parsed build information
         */
        fun parseBuildInfo(content: String): GoBuildInfo {
            var buildInfo = GoBuildInfo()
            val lines = content.lines()

            var currIndex = 0
            while (currIndex < lines.size) {
                val line = lines[currIndex].trim()
                if (line.isEmpty()) {
                    currIndex++
                    continue
                }

                val parts = line.split(" ", limit = 2)
                if (parts.size < 2) {
                    currIndex++
                    continue
                }

                when (parts[0]) {
                    PREFIX_MODULE -> {
                        buildInfo = buildInfo.copy(path = parts[1])
                    }

                    PREFIX_MOD -> {
                        // Read main module
                        val moduleLine = readModuleLine(lines, currIndex)
                        currIndex = moduleLine.second
                        buildInfo = buildInfo.copy(main = moduleLine.first)
                        continue
                    }

                    PREFIX_DEP -> {
                        // Read dependency module
                        val moduleLine = readModuleLine(lines, currIndex)
                        currIndex = moduleLine.second
                        val module = moduleLine.first
                        if (module != null) {
                            buildInfo = buildInfo.copy(deps = buildInfo.deps + module)
                        }
                        continue
                    }

                    PREFIX_BUILD -> {
                        // Read build setting
                        if (parts.size < 2 || !parts[1].contains(':')) {
                            throw IllegalArgumentException("Invalid build setting line: missing colon in key-value pair")
                        }

                        try {
                            val pair = extractKeyValue(parts[1])
                            buildInfo =
                                buildInfo.copy(settings = buildInfo.settings + GoBuildSetting(pair.first, pair.second))
                        } catch (e: IllegalArgumentException) {
                            throw IllegalArgumentException("Failed to parse build setting line: ${e.message}")
                        }
                    }
                }

                currIndex++
            }

            return buildInfo
        }

        /**
         * Reads a module line and processes its related lines (sum, replace).
         *
         * @param lines All content lines
         * @param startIndex Starting index in lines
         * @return Pair of parsed module and new line index
         */
        private fun readModuleLine(lines: List<String>, startIndex: Int): Pair<GoModule?, Int> {
            var index = startIndex

            if (index >= lines.size) {
                return null to index
            }

            // Parse the main module line
            val line = lines[index].trim()
            val parts = line.split(" ", limit = 2)
            if (parts.size < 2) {
                return null to (index + 1)
            }

            val prefix = parts[0]
            val moduleParts = parts[1].trim().split(" ", limit = 2)
            if (moduleParts.size < 2) {
                throw IllegalArgumentException("Invalid module line: missing version")
            }

            var module = GoModule(
                path = moduleParts[0],
                version = moduleParts[1]
            )

            index++

            // Process related lines (sum, replace)
            while (index < lines.size) {
                val nextLine = lines[index].trim()
                if (nextLine.isEmpty()) {
                    index++
                    continue
                }

                val nextParts = nextLine.split(" ", limit = 2)
                if (nextParts.size < 2) {
                    break
                }

                when (nextParts[0]) {
                    PREFIX_SUM -> {
                        module = module.copy(sum = nextParts[1])
                        index++
                    }

                    PREFIX_REPLACE -> {
                        // Process replacement module
                        val replaceParts = nextParts[1].trim().split(" ", limit = 2)
                        if (replaceParts.size < 2) {
                            throw IllegalArgumentException("Invalid replacement module: missing version")
                        }

                        val replaceModule = GoModule(
                            path = replaceParts[0],
                            version = replaceParts[1]
                        )

                        module = module.copy(replace = replaceModule)
                        index++
                    }

                    else -> break
                }
            }

            return when (prefix) {
                PREFIX_MOD, PREFIX_DEP -> module to index
                else -> null to index
            }
        }

        /**
         * Extracts key and value from a string in format "key:value".
         *
         * @param input The input string
         * @return Pair of key and value
         */
        private fun extractKeyValue(input: String): Pair<String, String> {
            val colonIndex = input.indexOf(':')
            if (colonIndex < 0) {
                throw IllegalArgumentException("Missing colon in key-value pair")
            }

            var key = input.substring(0, colonIndex).trim()
            var value = input.substring(colonIndex + 1).trim()

            // Handle quoted strings
            if (key.startsWith("\"")) {
                if (!key.endsWith("\"") || key.length < 2) {
                    throw IllegalArgumentException("Unclosed quoted key")
                }
                key = key.substring(1, key.length - 1)
            }

            if (value.startsWith("\"")) {
                if (!value.endsWith("\"") || value.length < 2) {
                    throw IllegalArgumentException("Unclosed quoted value")
                }
                value = value.substring(1, value.length - 1)
            }

            return key to value
        }

    }
} 