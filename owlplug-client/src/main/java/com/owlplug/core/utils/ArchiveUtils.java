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

public class ArchiveUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveUtils.class);

    public static void extract(final String source, final String dest) {
        final var sourceFile = new File(source);
        final var destDirectory = new File(dest);
        extract(sourceFile, destDirectory);
    }

    public static void extract(final File source, final File dest) {
        try {
            uncompress(source, dest);
        } catch (Exception e) {
            LOGGER.error("Error extracting archive {} at {}", source.getAbsolutePath(),
                    dest.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }

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

    private static void uncompress(final File sourceFile, final File destinationDirectory) throws IOException {
        if (isCompressed(sourceFile)) {
            try (InputStream fi = new FileInputStream(sourceFile);
                 InputStream bi = new BufferedInputStream(fi);
                 CompressorInputStream gzi = new CompressorStreamFactory().createCompressorInputStream(bi);
                 InputStream bgzi = new BufferedInputStream(gzi);
                 ArchiveInputStream<ArchiveEntry> o = new ArchiveStreamFactory().createArchiveInputStream(bgzi)) {

                uncompress(o, destinationDirectory);
            } catch (CompressorException e) {
                throw new IOException("Error while uncompressing the archive stream: " + sourceFile.getAbsolutePath(), e);
            } catch (ArchiveException e) {
                throw new IOException("Error while extracting the archive stream: " + sourceFile.getAbsolutePath(), e);
            }

        } else {
            try (InputStream fi = new FileInputStream(sourceFile);
                 InputStream bi = new BufferedInputStream(fi);
                 ArchiveInputStream<ArchiveEntry> o = new ArchiveStreamFactory().createArchiveInputStream(bi)) {

                uncompress(o, destinationDirectory);
            } catch (ArchiveException e) {
                throw new IOException("Error while extracting the archive stream: " + sourceFile.getAbsolutePath(), e);
            }
        }
    }

    private static void uncompress(ArchiveInputStream<ArchiveEntry> o, File destinationDirectory) throws IOException {

        ArchiveEntry entry;
        while ((entry = o.getNextEntry()) != null) {
            if (!o.canReadEntryData(entry)) {
                LOGGER.debug("Stream entry cannot be read: {}", entry.getName());
                continue;
            }

            File f = new File(destinationDirectory, entry.getName());
            if (entry.isDirectory()) {
                if (!f.isDirectory() && !f.mkdirs()) {
                    throw new IOException("failed to create directory " + f);
                }
            } else {
                File parent = f.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("failed to create directory " + parent);
                }
                try (OutputStream output = Files.newOutputStream(f.toPath())) {
                    IOUtils.copy(o, output);
                }
            }
        }
    }
}
