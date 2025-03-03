module space.iseki.executables.share {
    requires transitive kotlin.stdlib;
    exports space.iseki.executables.share to space.iseki.executables.pe, space.iseki.executables.elf;
}