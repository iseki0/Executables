module space.iseki.executables.elf {
    requires transitive kotlin.stdlib;
    requires transitive space.iseki.executables.common;
    requires transitive kotlinx.serialization.core;
    requires static kotlinx.serialization.json;
    requires space.iseki.executables.share;
    requires kotlinx.datetime;
    exports space.iseki.executables.elf;
} 