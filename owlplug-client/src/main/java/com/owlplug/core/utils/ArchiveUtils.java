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

package com.owlplug.core.utils;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ArchiveUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveUtils.class);

    /**
     * Extract the entire archive into the destination directory.
     *
     * @param archive the archive file to extract (path as string)
     * @param dest    the destination directory where the archive should be extracted (path as string)
     */
    public static void extract(final String archive, final String dest) {
        final var sourceFile = new File(archive);
        final var destDirectory = new File(dest);
        extract(sourceFile, destDirectory);
    }

    /**
     * Extract the entire archive into the destination directory.
     *
     * @param archive the archive file to extract
     * @param dest    the destination directory where the archive should be extracted
     */
    public static void extract(final File archive, final File dest) {
        try {
            uncompress(archive, dest);
        } catch (Exception e) {
            LOGGER.error("Error extracting archive {} at {}", archive.getAbsolutePath(),
                    dest.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract only specific files from an archive.
     * Backwards-compatible wrapper that uses the unified uncompress method.
     *
     * @param archive     the archive file to extract
     * @param dest        the destination directory where the archive should be extracted
     * @param targetPaths collection of entry paths to extract (relative paths inside the archive)
     */
    public static void extract(File archive, File dest, Collection<String> targetPaths) throws IOException {
        Objects.requireNonNull(targetPaths, "targetPaths cannot be null");
        Set<String> normalized = targetPaths.stream()
                .filter(Objects::nonNull)
                .map(ArchiveUtils::normalizeEntryName)
                .collect(Collectors.toSet());
        Predicate<String> filter = normalized::contains;
        uncompress(archive, dest, filter);
    }

    private static boolean isCompressed(final File file) throws IOException {
        LOGGER.debug("Verify file compression: {}", file.getAbsolutePath());
        try (InputStream inputStream = new FileInputStream(file);
             InputStream bufferedIn = new BufferedInputStream(inputStream)) {
            String comp = CompressorStreamFactory.detect(bufferedIn);
            LOGGER.debug("Compression signature found: {}", comp);
            return true;
        } catch (CompressorException e) {
            LOGGER.debug("Compression signature not found");
            return false;
        }

    }

    /**
     * Uncompress archive into destination (all entries).
     */
    private static void uncompress(File sourceFile, File destinationDirectory) throws IOException {
        uncompress(sourceFile, destinationDirectory, (Predicate<String>) null);
    }

    /**
     * Uncompress archive into destination but only entries accepted by the filter (if provided).
     * If the filter is null, all entries are extracted.
     */
    private static void uncompress(final File sourceFile, final File destinationDirectory, Predicate<String> filter) throws IOException {
        if (isCompressed(sourceFile)) {
            try (InputStream fi = new FileInputStream(sourceFile);
                 InputStream bi = new BufferedInputStream(fi);
                 CompressorInputStream gzi = new CompressorStreamFactory().createCompressorInputStream(bi);
                 InputStream bgzi = new BufferedInputStream(gzi);
                 ArchiveInputStream<ArchiveEntry> o = new ArchiveStreamFactory().createArchiveInputStream(bgzi)) {

                uncompress(o, destinationDirectory, filter);
            } catch (CompressorException e) {
                throw new IOException("Error while uncompressing the archive stream: " + sourceFile.getAbsolutePath(), e);
            } catch (ArchiveException e) {
                throw new IOException("Error while extracting the archive stream: " + sourceFile.getAbsolutePath(), e);
            }

        } else {
            try (InputStream fi = new FileInputStream(sourceFile);
                 InputStream bi = new BufferedInputStream(fi);
                 ArchiveInputStream<ArchiveEntry> o = new ArchiveStreamFactory().createArchiveInputStream(bi)) {

                uncompress(o, destinationDirectory, filter);
            } catch (ArchiveException e) {
                throw new IOException("Error while extracting the archive stream: " + sourceFile.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Core extraction from an ArchiveInputStream with optional filter and Zip Slip protection.
     */
    private static void uncompress(ArchiveInputStream<ArchiveEntry> ais, File destinationDirectory, Predicate<String> filter) throws IOException {
        // Ensure destination directory exists
        if (!destinationDirectory.exists() && !destinationDirectory.mkdirs()) {
            throw new IOException("Failed to create destination directory: " + destinationDirectory.getAbsolutePath());
        }

        String destCanonical = destinationDirectory.getCanonicalPath();
        if (!destCanonical.endsWith(File.separator)) {
            destCanonical = destCanonical + File.separator;
        }

        ArchiveEntry entry;
        while ((entry = ais.getNextEntry()) != null) {
            if (!ais.canReadEntryData(entry)) {
                LOGGER.debug("Stream entry cannot be read: {}", entry.getName());
                continue;
            }

            String entryName = normalizeEntryName(entry.getName());
            if (entryName == null || entryName.isEmpty()) {
                continue;
            }

            if (filter != null && !filter.test(entryName)) {
                continue;
            }

            File out = new File(destinationDirectory, entryName);

            // Zip Slip protection: check a canonical path
            String outCanonical = out.getCanonicalPath();
            if (!outCanonical.startsWith(destCanonical)) {
                throw new IOException("Entry is outside of the target dir: " + entry.getName());
            }

            if (entry.isDirectory()) {
                if (!out.isDirectory() && !out.mkdirs()) {
                    throw new IOException("failed to create directory " + out);
                }
            } else {
                File parent = out.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("failed to create directory " + parent);
                }
                try (OutputStream outStream = Files.newOutputStream(out.toPath())) {
                    IOUtils.copy(ais, outStream);
                }
            }
        }
    }

    /**
     * Normalize the entry name by replacing backslashes with forward slashes and removing leading slashes or drive letters.
     * This helps ensure consistent path handling across different platforms and archive formats.
     */
    private static String normalizeEntryName(String name) {
        if (name == null) {
            return null;
        }
        // Replace backslashes with forward slashes for consistent path handling
        String n = name.replace('\\', '/');
        // Remove leading slashes to prevent issues with absolute paths in archives
        while (n.startsWith("/")) {
            n = n.substring(1);
        }
        // Remove the drive letter if present (e.g., "C:/path/to/file")
        if (n.matches("^[A-Za-z]:/.*")) {
            n = n.substring(3);
        }
        return n;
    }
}
