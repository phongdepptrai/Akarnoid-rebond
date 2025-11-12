# Arkanoid Reborn

A modern, Swing-powered Arkanoid/Breakout remake that ships with its own lightweight 2D engine, full game loop, menu stack, localization, save profiles, an upgrade economy, and a growing catalogue of handcrafted levels.

## Feature Highlights
- **Scene-driven flow** - Menu, gameplay, world map, tutorial, pause, shop, settings, save selection, and profile scenes are all registered through `ArkanoidGame` for seamless transitions.
- **Deterministic engine** - Fixed timestep loop, buffered rendering, pooled audio, and responsive keyboard input keep gameplay predictable across platforms.
- **Dynamic levels & power-ups** - JSON-authored layouts define brick health, rewards, and objectives while runtime controllers (e.g., `PowerUpController`, `PaddleGunSystem`) spawn stretch, slow, multiball, and gun upgrades.
- **Progression & economy** - Currency, shop inventory, cosmetic skins, and unlock rules live under `gameplay` and `menu.shop`, enabling long-term progression.
- **Profiles & persistence** - Up to four save slots, automatic stat tracking, and profile personalization backed by JSON files in `data/profiles`.
- **Localization & audio** - Text lives in `src/main/resources/i18n/messages_*.json`; MP3/OGG assets plus a `SoundManager` and `Mp3AudioAdapter` bring retro ambience.

## Controls
- `Left` / `A` - move paddle left
- `Right` / `D` - move paddle right
- `Space` - launch the ball / confirm
- `Enter` - confirm menu selections
- `Esc` - pause gameplay or back out of menus

## Requirements
- **JDK 11+** (JRE for play, JDK for development)
- **Maven 3.6+**
- **Make** (optional, but all helper targets assume it)
- **Git** (recommended for syncing saves and source)

## Getting Started
```bash
git clone <repository-url>
cd BTL
make run
```
The launcher class is `com.arcade.arkanoid.ArcadeLauncher`, so IDEs can run that class directly if you prefer.

## Build & Run Commands

### Using Make (recommended)
```bash
make help     # list every helper target
make run      # compile (if needed) and launch the game
make build    # mvn compile
make test     # mvn test
make clean    # mvn clean
make package  # mvn package (fat JAR under target/)
make verify   # mvn verify (tests + extra checks)
make deps     # ensure Maven dependencies are downloaded
```

### Using Maven directly
```bash
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass=com.arcade.arkanoid.ArcadeLauncher
mvn test
mvn package
```
Use the provided Make targets whenever possible--those commands add friendly logging and keep the workflow identical on Windows, macOS, and Linux shells.

## Testing
- `make test` (or `mvn test`) runs the full JUnit 5 suite found under `src/test/java`.
- Tests rely on utilities inside `src/test/java/com/arcade/arkanoid/testutil` for building fake contexts.
- To add coverage, mirror the production package under `src/test/java`, annotate with `@Test`, and rely on the standard JUnit Jupiter assertions.
- CI-style verification (`make verify`) runs compilation, tests, and Surefire validation in one pass.

## Project Layout
```text
.
|-- pom.xml                 # Maven configuration
|-- Makefile                # Friendly wrapper around Maven targets
|-- src/
|   |-- main/
|   |   |-- java/com/arcade/arkanoid/
|   |   |   |-- engine/...    # Game loop, input, audio, scene system
|   |   |   |-- gameplay/...  # Entities, systems, power-ups, collision
|   |   |   |-- menu/...      # Menus, world map, shop, saves UI
|   |   |   `-- profile/...   # Profile manager & persistence helpers
|   |   `-- resources/
|   |       |-- levels/       # 001.json ... 013.json + manifest
|   |       |-- i18n/         # messages_en.json, messages_vi.json, etc.
|   |       |-- graphics/     # sprites, HUD art, paddle skins
|   |       |-- fonts/        # arcade font stack
|   |       `-- sounds/       # mp3 effects & music
|   `-- test/java/...         # JUnit 5 tests
|-- data/
|   |-- profiles/             # default-profile.json, save-slot-*.json
|   |-- saves/                # runtime state dumps
|   `-- screenshots/          # captured images
|-- build/                    # auxiliary build outputs (IDE integrations)
|-- target/                   # Maven artifacts
`-- lib/                      # Extra libraries (e.g., standalone JUnit console)
```

## Core Modules
- `engine.core` - `Game`, `GameLoop`, `GameConfig`, and timing utilities for deterministic updates.
- `engine.scene` - `SceneManager` plus lifecycle contracts shared by menus and gameplay scenes.
- `engine.input` - `InputManager` centralizes key state and higher-level actions.
- `engine.audio` - `SoundManager` with MP3 playback through `Mp3AudioAdapter`.
- `gameplay.entities` - `Paddle`, `Ball`, `Brick`, `PowerUp`, and supporting systems (guns, collisions, power-up timers).
- `menu.*` - `MainMenuScene`, `WorldMapScene`, `ShopScene`, `SaveMenuScene`, `ProfileDetailScene`, `TutorialScene`, etc.
- `profile` - `ProfileManager` and repositories for persisting stats, currency, and unlocks.

## Game Data & Customization
- **Levels** - JSON files in `src/main/resources/levels` define brick grids, durability, reward payouts, and scripted objectives. The `manifest.json` file declares the ordering shown on the world map. Copy an existing level (e.g., `007.json`), tweak the matrix, and reference it in the manifest to add new content.
- **Localization** - Each key lives in `src/main/resources/i18n/messages_<lang>.json`. Duplicate `messages_en.json`, translate values, and register the language inside the settings/profile UI to expose it at runtime.
- **Profiles & saves** - Runtime data is written to `data/profiles/save-slot-*.json`. Delete or edit these files to reset progress. `default-profile.json` acts as a template for new slots.
- **Audio & art** - Drop new assets into `src/main/resources/sounds` or `graphics` and reference them through the relevant manager (`SoundManager`, texture loaders). Fonts go under `resources/fonts`.
- **Settings** - Engine and gameplay tuning lives inside `GameSettings` under `engine.settings`. Expose new settings via the `SettingsScene` so they can be toggled without editing code.
- **Power-ups** - Extend `PowerUpType` and update `PowerUpController` plus any rendering classes to introduce new pick-ups. Systems such as `PaddleGunSystem` show the integration pattern.

## Development Workflow Tips
- Use `make run` while iterating: it recompiles only when sources change and relaunches the Swing window.
- When adjusting assets or localization JSON, re-run `make run` so Maven copies updated resources into `target/classes`.
- Keep saves under version control cautiously--`data/profiles` contains personal progression, so consider adding it to your `.git/info/exclude` if you do not want to commit runtime data.
- For debugging headless logic, unit tests under `src/test/java/com/arcade/arkanoid/menu` and `.../gameplay` show how to spin up scenes without rendering.

## Troubleshooting
- **`UnsupportedClassVersionError`** - Ensure `java -version` reports 11 or newer; older JVMs cannot run the compiled classes.
- **No audio on Linux/WSL** - Swing + Java Sound need an audio sink; run under a desktop session, or disable sound via settings if you are running headless.
- **Textures or translations missing** - Confirm you are launching from the project root so Maven's resource paths resolve correctly. `make clean && make run` fixes most stale classpath issues.
- **Corrupted saves** - Delete any problematic `data/profiles/save-slot-*.json` file; it will be regenerated from `default-profile.json`.

Happy brick breaking!



