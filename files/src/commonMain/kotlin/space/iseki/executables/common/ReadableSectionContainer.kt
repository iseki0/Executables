package space.iseki.executables.common

interface ReadableSectionContainer {

    /**
     * Get all sections of the container
     *
     * @return a list of sections, unmodifiable
     */
    val sections: List<ReadableSection>
}
