package space.iseki.executables.sbom

/**
 * Base exception for SBOM reading errors.
 */
open class SBomReadingException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Exception thrown when an SBOM cannot be found in a file.
 */
class SBomNotFoundException(message: String) : SBomReadingException(message)

/**
 * Exception specific to Go SBOM reading errors.
 */
class GoSBomReadingException(message: String, cause: Throwable? = null) : SBomReadingException(message, cause) 