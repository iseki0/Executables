# Project Structure Guide

## Directory Structure

```
.
├── all/                    # Main project module, depends on all submodules
├── common/                 # Common module with shared code
├── elf/                    # ELF file format support
│   └── src/commonMain/define/  # ELF-related enum and flag definitions
├── macho/                  # Mach-O file format support
│   └── src/commonMain/define/  # Mach-O-related enum and flag definitions
├── pe/                     # PE file format support
│   └── src/commonMain/define/  # PE-related enum and flag definitions
└── g/                      # Code generator module
    ├── src/main/kotlin/a/G.kt  # Generator main code
    └── src/main/resources/     # Generator templates
        ├── enum.ftl        # Enum generation template
        └── flag.ftl        # Flag set generation template
```

## Code Generator Usage Guide

### 1. Definition File Location

All enum and flag set definition files should be placed in the `src/commonMain/define/` directory of their respective
modules.

### 2. File Naming Rules

- Enum definition files: `*.enum.yml`
- Flag set definition files: `*.flag.yml`

### 3. Definition File Format

#### Enum Definition File (*.enum.yml)

```yaml
typename: Enum type name
package: Package name
dataLength: Data length in bytes
list:
  - name: Enum value name
    value: Enum value (hexadecimal)
    docs: Documentation
```

#### Flag Set Definition File (*.flag.yml)

```yaml
typename: Flag set type name
package: Package name
dataLength: Data length in bytes
list:
  - name: Flag name
    value: Flag value (hexadecimal)
    docs: Documentation
```

### 4. Code Generation

Run the following command to generate code:

```bash
./gradlew tGenerateFlagFiles
```

Generated code will be located in the `build/aao/` directory of each module.

### 5. Examples

#### Enum Definition Example

```yaml
typename: MachoFileType
package: space.iseki.executables.macho
dataLength: 4
list:
  - name: MH_OBJECT
    value: 0x1
    docs: relocatable object file
```

#### Flag Set Definition Example

```yaml
typename: MachoFlags
package: space.iseki.executables.macho
dataLength: 4
list:
  - name: MH_NOUNDEFS
    value: 0x1
    docs: the object file has no undefined references
```

### 6. Important Notes

1. All definition files must be placed in the correct directory
2. File names must follow the naming rules
3. Definition files must strictly follow YAML format
4. Verify all definition files are correctly formatted before generating code
5. Check the output in the `build/aao/` directory after generation

### 7. Common Tasks

1. Adding new enum definitions:
    - Create a new `.enum.yml` file in the appropriate module's `define` directory
    - Follow the enum definition format
    - Run the generator

2. Adding new flag set definitions:
    - Create a new `.flag.yml` file in the appropriate module's `define` directory
    - Follow the flag set definition format
    - Run the generator

3. Modifying existing definitions:
    - Edit the appropriate `.yml` file
    - Run the generator to update the generated code

4. Checking generated code:
    - Look in the module's `build/aao/` directory
    - Verify the generated Kotlin files are correct 