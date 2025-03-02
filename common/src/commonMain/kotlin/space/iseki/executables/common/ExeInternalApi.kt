package space.iseki.executables.common

/**
 * Marks declarations that are **internal** in the API, which means that should not be used outside of
 * this project, because their signatures and semantics will change between future releases without any
 * warnings and without providing any migration aids.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.TYPEALIAS, AnnotationTarget.PROPERTY)
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR, message = "This is an internal API that " +
            "should not be used from outside of this project. No compatibility guarantees are provided. " +
            "It is recommended to report your use-case of internal API to the issue tracker, " +
            "so stable API could be provided instead"
)

annotation class ExeInternalApi
