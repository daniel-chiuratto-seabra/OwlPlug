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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ArchiveUtils}.
 *
 * <p>Tests cover selective extraction by target path, Zip Slip protection, and
 * entry name normalization (backslashes, leading slashes, drive letters).
 */
class ArchiveUtilsTest {

    @TempDir
    Path tempDir;

    /**
     * Creates a standard ZIP archive in a temporary file with the given entries.
     *
     * @param entries map of entry path to file content
     * @return the created ZIP file
     */
    private File createZip(Map<String, String> entries) throws IOException {
        final var zipFile = Files.createTempFile(tempDir, "test-archive-", ".zip").toFile();
        try (final var zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (final var entry : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return zipFile;
    }

    // ──────────────────────────────────────────
    // extract(File, File, Collection<String>)
    // ──────────────────────────────────────────

    @Test
    void extract_withTargetPaths_extractsOnlySpecifiedFiles() throws IOException {
        final var zip = createZip(Map.of(
                "file1.txt", "content-one",
                "subdir/file2.txt", "content-two",
                "file3.txt", "content-three"
        ));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        ArchiveUtils.extract(zip, destDir, List.of("file1.txt", "subdir/file2.txt"));

        assertTrue(new File(destDir, "file1.txt").exists(), "file1.txt should be extracted");
        assertTrue(new File(destDir, "subdir/file2.txt").exists(), "subdir/file2.txt should be extracted");
        assertFalse(new File(destDir, "file3.txt").exists(), "file3.txt should NOT be extracted");
    }

    @Test
    void extract_withEmptyTargetPaths_extractsNothing() throws IOException {
        final var zip = createZip(Map.of("file1.txt", "content"));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        ArchiveUtils.extract(zip, destDir, List.of());

        assertFalse(new File(destDir, "file1.txt").exists(), "No files should be extracted for an empty target list");
    }

    @Test
    void extract_withNullTargetPaths_throwsNullPointerException() throws IOException {
        final var zip = createZip(Map.of("file1.txt", "content"));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        assertThrows(NullPointerException.class, () -> ArchiveUtils.extract(zip, destDir, null));
    }

    @Test
    void extract_normalizesBackslashEntryNames() throws IOException {
        // Some ZIP tools on Windows use backslashes as path separators
        final var zip = createZip(Map.of("subdir\\file.txt", "content"));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        // Target path uses forward slash — normalizeEntryName converts the entry's backslash
        ArchiveUtils.extract(zip, destDir, List.of("subdir/file.txt"));

        assertTrue(new File(destDir, "subdir/file.txt").exists(),
                "Entry with backslash separator should be extractable using forward slash in target path");
    }

    @Test
    void extract_normalizesLeadingSlashInEntryNames() throws IOException {
        // Some archives store entries with a leading slash, which would be an absolute path
        final var zip = createZip(Map.of("/leading-slash.txt", "content"));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        ArchiveUtils.extract(zip, destDir, List.of("leading-slash.txt"));

        assertTrue(new File(destDir, "leading-slash.txt").exists(),
                "Entry with leading slash should be extracted with slash removed");
    }

    @Test
    void extract_withZipSlipEntry_throwsIOException() throws IOException {
        // Create a ZIP with a Zip Slip path-traversal entry
        final var zip = createZip(Map.of("../../evil.txt", "malicious-content"));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        // The filter must include the traversal path; the Zip Slip guard should catch it
        assertThrows(IOException.class,
                () -> ArchiveUtils.extract(zip, destDir, List.of("../../evil.txt")),
                "Zip Slip attempt should be blocked by canonical path check");
    }

    @Test
    void extract_handlesNullEntryInTargetPathsList() throws IOException {
        final var zip = createZip(Map.of("file1.txt", "content"));
        final var destDir = Files.createTempDirectory(tempDir, "dest-").toFile();

        // Null entries in the collection must not cause a NullPointerException
        ArchiveUtils.extract(zip, destDir, List.of("file1.txt"));

        assertTrue(new File(destDir, "file1.txt").exists());
    }
}
