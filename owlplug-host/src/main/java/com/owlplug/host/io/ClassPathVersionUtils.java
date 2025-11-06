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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ClassPathVersionUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathVersionUtils.class);

    public static String getVersion(final String resource) throws IOException {
        final var filename = "%s.version".formatted(resource);
        try (final var inputStream = ClassPathVersionUtils.class.getClassLoader().getResourceAsStream(filename)) {
            if (inputStream == null) {
                throw new IOException("Resource %s not found".formatted(filename));
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static String getVersionSafe(final String resource) {
        try {
            return getVersion(resource);
        } catch (final IOException ioException) {
            LOGGER.error("Version can't be retrieved from resource {}", resource, ioException);
        }
        return "undefined-version";
    }
}
