# Gemini Project Overview: OwlPlug

## Project Overview

OwlPlug is a Java-based audio plugin manager for VST, VST3, Audio Units (AU), and LV2. It simplifies the management of audio plugins by providing a centralized interface to manage, organize, and discover plugins. The application is built with JavaFX for the UI, Spring Boot for the application framework, and uses a native C++ component built with the JUCE framework to interact with the audio plugin libraries.

## Building and Running

The project uses Maven for building the Java modules and shell scripts to build the native C++ component.

### Build Steps

1.  **Clone the repository with submodules:**
    ```sh
    git clone --recurse-submodules https://github.com/daniel-chiuratto-seabra/OwlPlug
    cd OwlPlug
    ```

2.  **Build the native `owlplug-host` component:**
    ```sh
    ./build/download-projucer.sh
    ./build/build-host-osx.sh
    ```

3.  **Build the Java modules with Maven:**
    ```sh
    mvn clean install
    ```

### Running the application

```sh
cd owlplug-client
mvn spring-boot:run
```

## Development Conventions

*   The project is structured into four Maven modules: `owlplug-client`, `owlplug-host`, `owlplug-controls`, and `owlplug-parsers`.
*   The `owlplug-client` module contains the main application logic and UI.
*   The `owlplug-host` module is the C++/JUCE component that interacts with audio plugins.
*   The `owlplug-controls` module contains custom JavaFX UI components.
*   The `owlplug-parsers` module is responsible for parsing DAW project files.

## Code Style

The project uses Checkstyle to enforce coding conventions. The configuration can be found in the `checkstyle.xml` file. Key conventions include:

*   **Line Length:** Maximum 120 characters.
*   **Indentation:** 2 spaces for indentation.
*   **Braces:** Braces are required for all blocks, including single-line `if` statements.
*   **Imports:** Star imports are not allowed.
*   **Naming Conventions:**
    *   Package names: `^[a-z]+(\.[a-z][a-z0-9]*)*$`
    *   Type names: `^[A-Z][a-zA-Z0-9]*$`
    *   Member names: `^[a-z][a-z0-9][a-zA-Z0-9]*$`
    *   Parameter names: `^[a-z]([a-z0-9][a-zA-Z0-9]*)?$`

## CI/CD

The project uses GitHub Actions for CI/CD. The workflow is defined in `.github/workflows/main.yml`. The pipeline consists of the following stages:

1.  **Build Native Host:** The `owlplug-host` native component is built for Windows, macOS, and Linux in parallel.
2.  **Build JAR:** The Java application is built, and the native host artifacts are included.
3.  **Package Installers:** Installers are created for Windows (.msi), macOS (.dmg), and Linux (.deb, .AppImage).
4.  **Release:** A draft release is created on GitHub with the packaged installers.
