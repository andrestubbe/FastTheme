# FastTheme Examples

This directory contains standalone examples demonstrating how to use the FastTheme module.

## Structure
- **Demo.java**: A side-by-side comparison of standard Swing vs. FastTheme native styling.
- **Demo2.java**: The "Wake Up" demo — transitions from Standard Swing to FastTheme at runtime via key press.

## Running the Demo
You can run the demo directly using Maven from this directory:

```bash
# Run the side-by-side comparison
mvn compile exec:java -Dexec.mainClass="fasttheme.Demo"

# Run the WakeUp transition demo
mvn compile exec:java -Dexec.mainClass="fasttheme.Demo2"
```

## Requirements
- **JDK 17+**
- **Windows 10/11** (for native DWM features)
- **FastCore**: Ensure FastCore is installed or available via JitPack.
