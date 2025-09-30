# Arkanoid Reborn

A modern, object-oriented Arkanoid clone built with Java Swing. The project ships with a lightweight 2D game engine that provides scene management, the render loop, input handling, and basic asset/sound plumbing.

## Highlights
- Scene-based architecture with reusable `Gameplay`, `MainMenu`, and `Pause` scenes.
- Deterministic fixed-timestep game loop, buffered rendering, and responsive keyboard input.
- Dynamic level loader with multiple stages, brick strengths, score tracking, and lives.
- Power-up system (expand paddle, slow ball), HUD overlay, pause menu, and keyboard-driven UI.
- **Modern build system**: Maven for dependency management with familiar Makefile interface.
- **Professional testing**: JUnit 5 integration with comprehensive test framework.
- **Cross-platform**: Works on Windows, Linux, macOS with consistent commands.

## Controls
- `Left` / `A`: move paddle left
- `Right` / `D`: move paddle right
- `Space`: launch ball / confirm
- `Enter`: confirm menu selections
- `Esc`: pause during gameplay, resume or navigate menus

## Building and Running

The project uses **Maven** for modern dependency management with a **familiar Makefile interface**. Choose the workflow that matches your preference:

### Option 1 - Make Commands (Recommended - All Platforms)
```bash
make run        # compile and run the game
make build      # compile sources using Maven
make test       # run JUnit 5 tests
make clean      # clean build artifacts
make package    # create distributable JAR
make verify     # run full test suite with validation
make help       # show all available commands
```

### Option 2 - Direct Maven Commands (Advanced)
```bash
mvn compile     # compile sources
mvn test        # run JUnit 5 tests
mvn exec:java   # run the game
mvn package     # create JAR file
mvn clean       # clean build artifacts
```

### Option 3 - Manual Java commands (Not recommended)
```bash
# Note: Maven handles classpath automatically, manual compilation not recommended
# Use 'make build' or 'mvn compile' instead
```

## Testing

The project uses **JUnit 5** with **Maven** for professional testing:

### Running Tests
```bash
make test       # Run all JUnit 5 tests (recommended)
mvn test        # Direct Maven test execution
```

### Test Structure
- **Framework**: JUnit 5 with full assertion library
- **Dependencies**: Managed automatically by Maven
- **Test Sources**: Place tests under `src/test/java/` using same package structure
- **IDE Integration**: Full VS Code and IntelliJ support
- **Continuous Testing**: Supports test-driven development workflow

### Adding New Tests
Create test classes in `src/test/java` following JUnit 5 conventions:
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class YourClassTest {
    @Test
    void testMethodName() {
        // Your test code here
        assertEquals(expected, actual);
    }
}
```

## System Requirements
- **Java 11+** (JDK for development, JRE for running)
- **Maven 3.6+** (automatically downloaded if using `mvnw` wrapper)
- **Make** (optional; for familiar make commands - works on all platforms)
- **Git** (recommended for version control)

## Project Layout
```
├── pom.xml                             # Maven configuration & dependencies
├── Makefile                            # Convenient wrapper around Maven commands
├── src/main/java/com/arcade/arkanoid/
│   ├── ArcadeLauncher.java             # Entry point
│   ├── ArkanoidGame.java               # Game bootstrap & scene registration
│   ├── engine/                         # Lightweight engine (core loop, scene manager, input, assets, audio)
│   ├── gameplay/                       # Gameplay logic, entities, levels, HUD rendering
│   └── menu/                           # Menu & pause scenes
├── src/test/java/                      # JUnit 5 test sources
├── target/                             # Maven build output
│   ├── classes/                        # Compiled main classes
│   └── test-classes/                   # Compiled test classes
└── docs/                               # Documentation (MAVEN_SETUP.md, BUILD_SYSTEM_MIGRATION.md)
```

## Development Workflow

### Quick Start
```bash
git clone <repository-url>
cd BTL
make run                # Compile and run the game
```

### Daily Development
```bash
make test               # Run tests during development
make build              # Compile changes
make run                # Test your changes
```

### Creating Releases
```bash
make package            # Create distributable JAR
make verify             # Run full validation suite
```

