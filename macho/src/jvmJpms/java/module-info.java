module space.iseki.executables.macho {
    requires transitive kotlin.stdlib;
    requires transitive space.iseki.executables.common;
    requires transitive kotlinx.serialization.core;
    requires kotlinx.datetime;
    requires space.iseki.executables.share;
    exports space.iseki.executables.macho;
} 