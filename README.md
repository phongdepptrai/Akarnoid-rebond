# Arkanoid Reborn

A modern, feature-rich Arkanoid clone built with Java Swing. The project includes a lightweight 2D game engine with comprehensive game systems including progression, economy, localization, and profile management.

## Highlights
- **Scene-based architecture**: Reusable scenes including `Gameplay`, `MainMenu`, `WorldMap`, `Pause`, and save management.
- **Game engine**: Deterministic fixed-timestep game loop, buffered rendering, responsive input handling, and asset management.
- **Dynamic level system**: Multiple stages with varying brick strengths, objectives, and progression tracking.
- **Power-up system**: Expand paddle, slow ball, multi-ball, and more with visual effects.
- **Economy & progression**: Currency system, shop, upgrades, and unlockable content.
- **Profile management**: Multiple save slots with persistent game state and statistics.
- **Localization support**: Multi-language support with JSON-based translation system.
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
├── src/
│   ├── main/
│   │   ├── java/com/arcade/arkanoid/
│   │   │   ├── ArcadeLauncher.java    # Application entry point
│   │   │   ├── ArkanoidGame.java      # Game bootstrap & scene registration
│   │   │   ├── core/                  # Core utilities and project paths
│   │   │   ├── economy/               # Currency, shop, and economy system
│   │   │   ├── engine/                # Lightweight 2D game engine
│   │   │   │   ├── assets/            # Asset loading and management
│   │   │   │   ├── audio/             # Audio system and sound effects
│   │   │   │   ├── core/              # Core game loop and configuration
│   │   │   │   ├── input/             # Keyboard and input handling
│   │   │   │   ├── scene/             # Scene management system
│   │   │   │   ├── settings/          # Game settings and preferences
│   │   │   │   └── util/              # Engine utilities
│   │   │   ├── gameplay/              # Core gameplay systems
│   │   │   │   ├── entities/          # Game entities (Ball, Paddle, Brick, etc.)
│   │   │   │   ├── levels/            # Level loading and management
│   │   │   │   ├── objectives/        # Level objectives and completion tracking
│   │   │   │   └── system/            # Gameplay systems (physics, collision, etc.)
│   │   │   ├── localization/          # Multi-language support system
│   │   │   ├── menu/                  # Menu scenes and UI
│   │   │   │   ├── save/              # Save/load game functionality
│   │   │   │   └── worldmap/          # World map and level selection
│   │   │   └── profile/               # Player profile and progression
│   │   └── resources/
│   │       ├── fonts/                 # Game fonts
│   │       ├── graphics/              # Sprites, textures, and UI assets
│   │       ├── i18n/                  # Translation files (JSON)
│   │       │   └── messages_en.json   # English translations
│   │       └── levels/                # Level definitions (JSON)
│   │           ├── manifest.json      # Level manifest and metadata
│   │           └── 001.json - 013.json # Individual level files
│   └── test/java/                     # JUnit 5 test sources
├── data/                               # Runtime data directory
│   ├── profiles/                      # Player profile save files
│   │   ├── default-profile.json       # Default profile template
│   │   └── save-slot-*.json           # Player save slots (1-4)
│   ├── saves/                         # Game save states
│   └── screenshots/                   # Screenshot storage
├── target/                            # Maven build output
│   ├── classes/                       # Compiled main classes + resources
│   ├── test-classes/                  # Compiled test classes
│   └── surefire-reports/              # Test execution reports
├── build/                             # Additional build artifacts
└── lib/                               # External libraries (if any)
```

## Key Features

### 🎮 Gameplay Systems
- **Dynamic Level System**: 13+ levels with progressive difficulty and unique layouts
- **Power-ups**: Multiple power-ups including paddle expansion, ball slowdown, multi-ball, and more
- **Objectives**: Level-specific objectives and completion tracking
- **Physics**: Realistic ball physics with collision detection and response

### 💰 Economy & Progression
- **Currency System**: Earn coins by completing levels and achieving objectives
- **Shop**: Purchase upgrades, power-ups, and cosmetic items
- **Upgrades**: Permanent upgrades that enhance gameplay (stronger paddle, extra lives, etc.)
- **Unlockables**: Unlock new levels, power-ups, and features through progression

### 👤 Profile Management
- **Multiple Save Slots**: Up to 4 independent player profiles
- **Persistent Progress**: Automatic saving of level completion, currency, and unlocks
- **Statistics Tracking**: Track high scores, total playtime, and achievements
- **Profile Customization**: Personalize player name and settings per profile

### 🌍 Localization
- **Multi-language Support**: Extensible JSON-based translation system
- **Easy Translation**: Add new languages by creating translation JSON files
- **Dynamic Language Switching**: Change language without restarting the game

### 🎨 Visual & Audio
- **Retro Graphics**: Classic arcade-style pixel art and animations
- **Sound Effects**: Immersive audio feedback for actions and events
- **Particle Effects**: Visual effects for power-ups and special events
- **Smooth Animations**: 60 FPS gameplay with interpolated rendering

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

## Architecture Overview

### Engine Layer
The custom 2D game engine provides:
- Fixed-timestep game loop with delta time interpolation
- Scene-based state management with transitions
- Asset loading and caching system
- Input handling with key mapping
- Audio system with sound effect management
- Configuration and settings persistence

### Game Layer
Built on top of the engine:
- Entity-component architecture for game objects
- Level loading from JSON definitions
- Collision detection and physics simulation
- HUD and UI rendering system
- Save/load functionality
- Profile and progression tracking

### Module Organization
- **`core/`**: Project-wide utilities and path management
- **`economy/`**: Currency, shop, and purchase system
- **`engine/`**: Reusable game engine components
- **`gameplay/`**: Game-specific logic and entities
- **`localization/`**: Translation and multi-language support
- **`menu/`**: Menu scenes and UI components
- **`profile/`**: Player data and progression management

