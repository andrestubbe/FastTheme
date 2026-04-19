# Building from Source

## Prerequisites

- JDK 17+
- Maven 3.9+
- **Windows:** Visual Studio 2019+ or Build Tools

## Build

### Windows

```bash
compile.bat
mvn clean package
```

The build script compiles the native library and packages it with the JAR.

## Run Examples

```bash
cd examples/00-basic-usage
mvn compile exec:java
```

## Installation

### JitPack (Recommended)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.andrestubbe</groupId>
        <artifactId>fasttheme</artifactId>
        <version>v1.3.1</version>
    </dependency>
</dependencies>
```

### Gradle (JitPack)

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.andrestubbe:fasttheme:v1.3.1'
}
```

## Download Pre-built JAR

See [Releases Page](https://github.com/andrestubbe/FastTheme/releases)

## Troubleshooting

### JNI UnsatisfiedLinkError

If you get `UnsatisfiedLinkError`, the native library was not found:

1. Ensure `compile.bat` was run successfully
2. Check that the DLL exists in the project root or `build/`
3. Ensure the DLL is in PATH or copy to `C:\Windows\System32`
