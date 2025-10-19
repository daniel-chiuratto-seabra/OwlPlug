### **Complete OwlPlug Project Onboarding**

Welcome to OwlPlug! This is a detailed guide for you to understand the architecture, purpose, and how to contribute to the project.

#### **1. What is OwlPlug?**

OwlPlug is an **audio plugin manager**. Think of it as a "package manager" (like `npm` or `Homebrew`), but focused on audio plugins such as **VST, VST3, Audio Units (AU), and LV2**.

The main goal is to simplify the lives of music producers and audio engineers by centralizing the following tasks:

*   **Manage:** Detects and lists all plugins already installed on your computer.
*   **Organize:** Allows you to unify plugins from multiple folders into a single location using symbolic links, without needing to move the files physically.
*   **Analyze DAW Projects:** Scans project files from Digital Audio Workstations (like Reaper, Ableton, etc.) to identify which plugins are used and which are missing.
*   **Discover and Install:** Connects to online "registries" (plugin sources) to browse, download, and install new plugins automatically.

#### **2. Architecture and Technologies**

OwlPlug is a hybrid project, combining a desktop application made in Java with a native C++ component to handle the specifics of audio plugins.

The technology stack is:

*   **Main Language:** **Java 21**.
*   **Application Framework:** **Spring Boot**, which facilitates the configuration and execution of the application.
*   **Graphical Interface (UI):** **JavaFX**, for building the user interface.
*   **Database:** **H2** (an embedded database) with **Hibernate** (for object-relational mapping), used to store information about plugins, settings, etc.
*   **Build System:** **Maven**, which manages the dependencies and the compilation process of the Java project.
*   **Native Component:** **C++** with the **JUCE** framework. This part is essential because Java cannot interact directly with audio plugin libraries (which are `.dll`, `.vst3`, `.component`, `.so`). The JUCE component acts as a "bridge".

#### **3. Project Structure (Maven Modules)**

The project is divided into four main modules, which you can see in the root `pom.xml`:

*   `owlplug-client`: This is the heart of the project. It is the main desktop application with which the user interacts. It contains all the business logic, the graphical interface (`.fxml` files in `src/main/resources/fxml`), and communication with the other modules.
*   `owlplug-host`: This is the native component (C++/JUCE) responsible for scanning, loading, and extracting information from audio plugin files. It is compiled for each operating system (Windows, macOS, Linux) and packaged inside `owlplug-client`.
*   `owlplug-controls`: Contains custom UI components for JavaFX, creating a unique visual identity for OwlPlug.
*   `owlplug-parsers`: Module responsible for parsing the project files of DAWs. It uses a tool called ANTLR to interpret the structure of these files and extract the list of plugins used.

#### **4. How to Compile and Build the Project**

The build process has two main steps: compiling the native code (C++) and compiling the Java code. The automation scripts in GitHub Actions (`.github/workflows/main.yml`) give us the exact step-by-step process.

**Prerequisites on your macOS:**

1.  **JDK 21:** The project requires Java 21.
2.  **Maven:** To manage the Java build.
3.  **Xcode Command Line Tools:** Essential for compiling the C++ code of `owlplug-host`. Install with `xcode-select --install`.

**Step-by-Step for Local Build:**

1.  **Clone the Repository:**
    ```sh
    git clone --recurse-submodules https://github.com/DropSnorz/OwlPlug.git
    cd OwlPlug
    ```
    *(The `--recurse-submodules` is important to download the JUCE dependencies).*\

2.  **Build `owlplug-host` (Native Component):**
    The project already comes with scripts to automate this. For macOS, run:
    ```sh
    # First, download the Projucer tool needed for the JUCE build
    ./build/download-projucer.sh
    
    # Now, run the build script for macOS
    ./build/build-host-osx.sh
    ```
    This command will compile the C++ code and generate a dynamic library (`.dylib`) in `owlplug-host/src/main/juce/Builds/MacOSX/build/Release/`. The Maven build system is configured to find this file.

3.  **Build the Java Modules with Maven:**
    Now, use Maven to compile all the Java code and package the artifacts.
    ```sh
    mvn clean install
    ```
    This command will:
    *   Download all Java dependencies.
    *   Compile the `owlplug-controls`, `owlplug-parsers`, `owlplug-host` (its Java part), and finally, the `owlplug-client` modules.
    *   The tests will be run (you can skip them with `-DskipTests` if needed).

#### **5. How to Run the Application in Development Mode**

After the build (`mvn clean install`), you can run the application directly via Maven, which is great for development and quick tests.

```sh
# Navigate to the client directory
cd owlplug-client

# Use the Spring Boot plugin to start the application
mvn spring-boot:run
```

This will start OwlPlug, and you will see the graphical interface appear on your screen.

#### **6. How to Generate the Executable File (`.jar`)**

If you want to generate a single `.jar` file that can be run anywhere (as long as Java 21 is installed), use the `repackage` command from Spring Boot.

```sh
# From the project root directory, or in the owlplug-client directory
cd owlplug-client
mvn clean package spring-boot:repackage
```

At the end, you will find the `owlplug-client-X.Y.Z.jar` file (where X.Y.Z is the version) inside the `owlplug-client/target/` folder. You can run it with:

```sh
java -jar owlplug-client-X.Y.Z.jar
```

#### **7. How to Create the Installer for macOS (`.dmg`)**

The project also includes scripts to create a native installer, which embeds Java and does not require the user to have it installed.

1.  **Make sure the `.jar` has been generated:** Follow step 6 to ensure the executable `.jar` file exists in `owlplug-client/target/`.

2.  **Run the Packaging Script:**
    The `package-dmg.sh` script in the `build/` folder was made for this. It uses the `jpackage` tool from the JDK to create the `.dmg`.

    ```sh
    # From the project root
    cd build
    
    # Execute the script passing the project version as an argument
    # Ex: ./package-dmg.sh 1.30.1
    ./package-dmg.sh $(mvn -q -Dexec.executable=echo -Dexec.args=\'${project.version}\' --non-recursive exec:exec)
    ```
    *The `mvn ...` command is a trick to get the version directly from the `pom.xml`.*\

3.  **Find the Installer:**
    After running the script, the `OwlPlug-X.Y.Z.dmg` file will be available in the `build/output/` folder.
