/* OwlPlug
 * Copyright (C) 2021 Arthur <dropsnorz@gmail.com>
 *
 * This file is part of OwlPlug.
 *
 * OwlPlug is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * as published by the Free Software Foundation.
 *
 * OwlPlug is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OwlPlug.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.owlplug.host.io;

import com.owlplug.host.model.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static java.nio.file.FileSystems.getDefault;

/**
 * Custom Library Loader.
 * Loads a Native library from a regular library path or classpath.
 *
 */
public final class LibraryLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LibraryLoader.class);

    private static final String SEPARATOR = getDefault().getSeparator();
    private static final Path TMP_PATH = Paths.get(System.getProperty("java.io.tmpdir"));
    private static final String LIB_EXTENSION = platformExtension();

    private LibraryLoader() {
    }

    public static boolean load(String libName, Class<?> ref, boolean throwOnFailure) {
        if (tryLoadByName(libName)) {
            return true;
        }
        final var tmpPath = TMP_PATH.resolve(libName + LIB_EXTENSION);
        return extractAndLoad(ref, libName, tmpPath);
    }

    private static boolean tryLoadByPath(final Path path) {
        if (!Files.exists(path)) {
            return false;
        }

        try {
            System.load(path.toAbsolutePath().toString());
            LOGGER.info("Loaded library from path: {}", path);
            return true;
        } catch (final UnsatisfiedLinkError unsatisfiedLinkError) {
            LOGGER.debug("Can't load library from path {} : {}", path, unsatisfiedLinkError.getMessage());
            LOGGER.trace("Library from path {} cannot be loaded", path, unsatisfiedLinkError);
            return false;
        }
    }

    /**
     * Loads a library from the path or by the library name.
     *
     * @param libName library path or name
     * @return {@code boolean} with {@code true} when successfully loaded otherwise {@code false}
     */
    public static boolean tryLoadByName(final String libName) {
        try {
            if (libName.contains(SEPARATOR)) {
                System.load(libName);
            } else {
                System.loadLibrary(libName);
            }
            LOGGER.info("Library {} successfully loaded", libName);
            return true;
        } catch (UnsatisfiedLinkError e) {
            LOGGER.debug("Can't load library {} : {}", libName, e.getMessage());
            LOGGER.trace("Library {} cannot be loaded", libName, e);

        }

        return false;
    }

    /**
     * Extracts and load the library from a temp directory.
     *
     * @param ref     JNI Class
     * @param libName library name
     * @return {@code boolean} with {@code true} when successfully extracted otherwise {@code false}
     */
    public static boolean extractAndLoad(final Class<?> ref, final String libName, final Path target) {
        String resourceName = libName + LIB_EXTENSION;
        try (InputStream in = ref.getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                LOGGER.debug("Library {} not found on classpath", resourceName);
                return false;
            }

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            LOGGER.debug("Extracted library to {}", target);

            return tryLoadByPath(target);
        } catch (IOException e) {
            LOGGER.error("Failed to extract native library {}", resourceName, e);
            return false;
        }
    }

    /**
     * Returns platform default library extension.
     * - Windows host: .dll
     * - Mac host: .dylib
     * Returns an empty string for any other hosts
     *
     * @return host default library extension
     */
    private static String platformExtension() {
        if (OS.WINDOWS.isCurrentOs()) return ".dll";
        if (OS.MAC.isCurrentOs()) return ".dylib";
        if (OS.LINUX.isCurrentOs()) return ".so";
        throw new UnsupportedOperationException(
                "Unsupported OS for native libraries: " + OS.current());
    }
}
