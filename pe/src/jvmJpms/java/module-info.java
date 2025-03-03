module space.iseki.executables.pe {
    requires transitive kotlin.stdlib;
    requires transitive space.iseki.executables.common;
    requires transitive kotlinx.serialization.core;
    requires kotlinx.datetime;
    exports space.iseki.executables.pe;
    exports space.iseki.executables.pe.vi;
}
