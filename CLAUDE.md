# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What is OwlPlug?

OwlPlug is an audio plugin manager for VST, VST3, AU, and LV2 plugins. It lets music producers manage, organize, and discover audio plugins — similar to a package manager but for audio plugins. It can scan DAW projects (Reaper, Ableton, etc.) to find used/missing plugins and connect to online registries to install new ones.

## Build Commands

Clone with submodules (JUCE dependency):
```sh
git clone --recurse-submodules <repo-url>
```

Build the native C++ component (required before Java build on macOS):
```sh
./build/download-projucer.sh
./build/build-host-osx.sh
```

Build all Java modules:
```sh
mvn clean install
```

Run the application in dev mode:
```sh
cd owlplug-client && mvn spring-boot:run
```

Generate executable JAR:
```sh
cd owlplug-client && mvn clean package spring-boot:repackage
# Output: owlplug-client/target/owlplug-client-X.Y.Z.jar
```

Create macOS installer:
```sh
cd build && ./package-dmg.sh 1.30.1
# Output: build/output/OwlPlug-X.Y.Z.dmg
```

Skip tests during build:
```sh
mvn clean install -DskipTests
```

## Test Commands

Run all tests:
```sh
mvn test
```

Run tests in a specific module:
```sh
cd owlplug-client && mvn test
```

Run a specific test class:
```sh
mvn test -Dtest=FileUtilsTest
```

Run a specific test method:
```sh
mvn test -Dtest=FileUtilsTest#methodName
```

Tests run headless using Monocle (glass platform) — no display required. The surefire plugin in `owlplug-client/pom.xml` configures `testfx.headless=true`, `glass.platform=Monocle`, and `prism.order=sw` automatically.

## Project Structure

Four Maven modules defined in root `pom.xml`:

- **`owlplug-client`** — Main JavaFX desktop app. Entry point: `com.owlplug.Bootstrap` → `com.owlplug.OwlPlug`. Contains all business logic, UI (FXML files in `src/main/resources/fxml`), Spring Boot services, JPA repositories, and async task system.
- **`owlplug-host`** — Native C++/JUCE component. Bridges Java and audio plugin libraries (`.dll`, `.vst3`, `.component`, `.so`). Compiled per-platform and embedded in owlplug-client resources.
- **`owlplug-controls`** — Custom reusable JavaFX UI components used by owlplug-client.
- **`owlplug-parsers`** — ANTLR 4-based parsers for DAW project files (currently Reaper).

## Architecture

**owlplug-client** package layout under `com.owlplug/`:
- `core/` — Utilities, base UI, task execution, shared services
- `plugin/` — Plugin management logic (scan, install, organize via symlinks)
- `project/` — DAW project scanning and analysis
- `auth/` — Google OAuth2 authentication
- `explore/` — Plugin registry browsing and downloads

**Key patterns:**
- Spring Boot beans for DI across services, repositories, and controllers
- JavaFX MVC: FXML for views, Spring-managed controllers for logic
- Spring Data JPA repositories for all data access (H2 embedded database, stored in `~/.owlplug/owlplug`)
- Custom async task system for long-running operations (plugin scans, downloads)
- `OwlPlugPreloader` shows splash screen during Spring Boot startup

## Code Style (enforced by Checkstyle)

- Max line length: 120 characters
- Indentation: 2 spaces
- Braces required on all blocks (including single-line `if`)
- No star imports
- Naming: `lowerCamelCase` for members/parameters, `UpperCamelCase` for types, lowercase dotted for packages

Run style check:
```sh
mvn checkstyle:check
```

## Tech Stack

- **Java 21**, **Spring Boot 3.x**, **JavaFX 21**
- **H2** embedded database + **Hibernate** ORM (`ddl-auto: update`)
- **ANTLR 4** for DAW file parsing
- **Apache Jena** for LV2 plugin RDF metadata
- **dd-plist** for macOS plist parsing
- **ControlsFX** + **JMetro** for UI components and theming
- **Lombok** for annotation-based code generation
- **TestFX** + **JUnit 5** + **Mockito** for testing