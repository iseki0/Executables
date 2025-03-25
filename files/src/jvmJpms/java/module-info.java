module space.iseki.executables.files {
    requires java.base;
    requires kotlin.stdlib;
    requires kotlinx.serialization.json;
    requires kotlinx.serialization.core;
    requires kotlinx.datetime;
    exports space.iseki.executables.common;
    exports space.iseki.executables.elf;
    exports space.iseki.executables.pe;
    exports space.iseki.executables.macho;
}
