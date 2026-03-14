package com.owlplug.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.owlplug.util.PropertyUtils.DEFAULT_PRELOADER;
import static java.lang.String.join;
import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PropertyUtilsTest {
    private Path tempYaml;
    private Path backupYaml;
    private Path originalYamlPath;

    @BeforeEach
    void setUp() throws IOException {
        // The class loader looks for resources in `target/classes` during maven test execution
        originalYamlPath = Path.of("target/classes/application.yaml");
        Files.createDirectories(originalYamlPath.getParent());

        if (Files.exists(originalYamlPath)) {
            backupYaml = Path.of(originalYamlPath.toString() + ".bak");
            Files.move(originalYamlPath, backupYaml, StandardCopyOption.REPLACE_EXISTING);
        }
        // Create a new empty file to be used in tests
        tempYaml = Files.createFile(originalYamlPath);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(originalYamlPath);
        if (backupYaml != null && Files.exists(backupYaml)) {
            Files.move(backupYaml, originalYamlPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    void getPreLoader_shouldReturnPreloaderClassFromYaml() throws IOException {
        final var preloaderClass = "com.owlplug.TestPreloader";
        final var content = join(lineSeparator(), "javafx:", "  pre-loader: " + preloaderClass);
        Files.writeString(tempYaml, content);
        assertEquals(preloaderClass, PropertyUtils.getPreLoader());
    }

    @Test
    void getPreLoader_shouldReturnDefaultWhenFileIsMissing() throws IOException {
        Files.delete(tempYaml);
        assertEquals(DEFAULT_PRELOADER, PropertyUtils.getPreLoader());
    }

    @Test
    void getPreLoader_shouldReturnDefaultWhenPropertyIsMissing() throws IOException {
        String content = "some: other-property";
        Files.writeString(tempYaml, content);
        assertEquals(DEFAULT_PRELOADER, PropertyUtils.getPreLoader());
    }

    @Test
    void getPreLoader_shouldReturnDefaultWhenPropertyIsBlank() throws IOException {
        final var content = join("\n", "javafx:", "  pre-loader: \"\"");
        Files.writeString(tempYaml, content);
        assertEquals(DEFAULT_PRELOADER, PropertyUtils.getPreLoader());
    }

    @Test
    void getPreLoader_shouldReturnDefaultWhenYamlIsMalformed() throws IOException {
        Files.writeString(tempYaml, "javafx: { pre-loader: unterminated string");
        assertEquals(DEFAULT_PRELOADER, PropertyUtils.getPreLoader());
    }
}
